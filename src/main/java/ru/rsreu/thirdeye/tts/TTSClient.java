package ru.rsreu.thirdeye.tts;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TTSClient {

        private final String apiUrl;

        public TTSClient(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        /**
         * Отправляет текст на синтез речи и сохраняет полученный WAV файл
         * @param text текст для синтеза
         * @param outputFilePath путь для сохранения WAV файла
         * @return true если успешно, false в случае ошибки
         */
        public boolean synthesizeSpeech(String text, String outputFilePath) {
            HttpURLConnection connection = null;
            try {
                // Создаем соединение
                URL url = new URL(apiUrl);
                connection = (HttpURLConnection) url.openConnection();

                // Настраиваем запрос
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "audio/wav");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                // Отправляем JSON данные
                String jsonInputString = "{\"text\": \"" + escapeJsonString(text) + "\"}";

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Проверяем код ответа
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Получаем и сохраняем WAV файл
                    try (InputStream inputStream = connection.getInputStream();
                         FileOutputStream outputStream = new FileOutputStream(outputFilePath)) {

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    System.out.println("WAV файл успешно сохранен: " + outputFilePath);
                    return true;
                } else {
                    System.err.println("Ошибка сервера: " + responseCode);
                    // Читаем сообщение об ошибке
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        System.err.println("Сообщение об ошибке: " + response.toString());
                    }
                    return false;
                }

            } catch (Exception e) {
                System.err.println("Ошибка при выполнении запроса: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        /**
         * Отправляет текст на синтез речи и возвращает WAV файл как массив байт
         * @param text текст для синтеза
         * @return массив байт WAV файла или null в случае ошибки
         */
        public byte[] synthesizeSpeechToBytes(String text) {
            HttpURLConnection connection = null;
            try {
                // Создаем соединение
                URL url = new URL(apiUrl);
                connection = (HttpURLConnection) url.openConnection();

                // Настраиваем запрос
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "audio/wav");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                // Отправляем JSON данные
                String jsonInputString = "{\"text\": \"" + escapeJsonString(text) + "\"}";

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Проверяем код ответа
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Читаем WAV файл в массив байт
                    try (InputStream inputStream = connection.getInputStream();
                         ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        System.out.println("WAV файл успешно получен, размер: " + outputStream.size() + " байт");
                        return outputStream.toByteArray();
                    }
                } else {
                    System.err.println("Ошибка сервера: " + responseCode);
                    return null;
                }

            } catch (Exception e) {
                System.err.println("Ошибка при выполнении запроса: " + e.getMessage());
                e.printStackTrace();
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        /**
         * Экранирует специальные символы в JSON строке
         */
        private String escapeJsonString(String text) {
            return text.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }

    /**
     * Сохраняет WAV файл в папку resources проекта
     * @param text текст для синтеза
     * @param fileName имя файла (например: "output.wav")
     * @return true если успешно
     */
    public boolean synthesizeSpeechToResources(String text, String fileName) {
        String resourcesPath = findOrCreateResourcesFolder();
        String outputFilePath = resourcesPath + File.separator + fileName;
        return synthesizeSpeech(text, outputFilePath);
    }

    /**
     * Сохраняет WAV файл в подпапку resources (например: "audio/hello.wav")
     * @param text текст для синтеза
     * @param subfolder подпапка (например: "audio")
     * @param fileName имя файла (например: "hello.wav")
     * @return true если успешно
     */
    public boolean synthesizeSpeechToResources(String text, String subfolder, String fileName) {
        String resourcesPath = findOrCreateResourcesFolder();
        String subfolderPath = resourcesPath + File.separator + subfolder;

        // Создаем подпапку если не существует
        File subfolderDir = new File(subfolderPath);
        if (!subfolderDir.exists()) {
            subfolderDir.mkdirs();
        }

        String outputFilePath = subfolderPath + File.separator + fileName;
        return synthesizeSpeech(text, outputFilePath);
    }


    private String findOrCreateResourcesFolder() {
        String[] possiblePaths = {
                System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources",
                System.getProperty("user.dir") + File.separator + "src" + File.separator + "resources",
                System.getProperty("user.dir") + File.separator + "resources"
        };

        for (String path : possiblePaths) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                return path;
            }
        }

        // Создаем стандартную Maven структуру
        String defaultPath = possiblePaths[0];
        new File(defaultPath).mkdirs();
        return defaultPath;
    }

//    public boolean synthesizeSpeechToResources(String text, String fileName) {
//        // Путь к папке resources в Maven проекте
//        String resourcesPath = System.getProperty("user.dir") +
//                File.separator + "src" +
//                File.separator + "main" +
//                File.separator + "resources" +
//                File.separator + "audiofile";
//
//        // Создаем папку, если не существует
//        File resourcesDir = new File(resourcesPath);
//        if (!resourcesDir.exists()) {
//            resourcesDir.mkdirs();
//        }
//
//        String outputFilePath = resourcesPath + File.separator + fileName;
//        return synthesizeSpeech(text, outputFilePath);
//    }

}
