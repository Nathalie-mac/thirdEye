package ru.rsreu.thirdeye;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.videoio.VideoCapture;

import java.util.*;

public class Main  extends Application {

    private Thread detectionThread = null;

    private volatile boolean cameraActive = false; // Флаг для состояния камеры
    private VideoCapture capture = new VideoCapture(); // Объект для захвата видео
    private ObjectDetector objectDetector = new ObjectDetector(); // Объект для распознавания объектов
    //private volatile boolean running = false;

    private Set<Integer> countedObjectIds; //
    private Map<String, Integer> objectTypeCount;
    private Set<TrackedObject> detectedObjects = new HashSet<>();//

    public static List<String> labels;
    public static int amountOfClasses;
    public static Scalar[] colors;
    public static Net network;
    public static int amountOfOutputLayers;
    public static List<String> outputLayersNames;

    @FXML private ImageView imageView;
    @FXML private Button cameraButton;

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/rsreu/thirdeye/hello-view.fxml"));
            BorderPane root = (BorderPane) loader.load();

            // Получаем контроллер из FXMLLoader
            Main controller = loader.getController();

            // Теперь переменная controller ссылается на текущий экземпляр контроллера

            Scene scene = new Scene(root, 800, 600);
            primaryStage.setTitle("Object detection and recognition");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    public static void main(String[] args) {
        // Загружаем нативную библиотеку OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String path = "src/main/resources/ru/rsreu/thirdeye/coco.names";
        labels = Services.formLabels(path);
        amountOfClasses = labels.size();

        //генерируем цвета рамок для каждого класса
        Random rnd = new Random();
        colors = new Scalar[amountOfClasses];
        for (int i = 0; i<amountOfClasses; i++){
            colors[i] = new Scalar(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        }

        //Инициализируем Yolov8
        String cfgPath = "src/main/resources/ru/rsreu/thirdeye/yolov4.cfg";
        String weightsPath = "src/main/resources/ru/rsreu/thirdeye/yolov4-tiny.weights";
        network = Dnn.readNetFromDarknet(cfgPath, weightsPath);

        List<String>  namesOfLayers = network.getLayerNames();

        MatOfInt outputLayersIndices = network.getUnconnectedOutLayers();
        amountOfOutputLayers = outputLayersIndices.toArray().length;
        outputLayersNames = new ArrayList();
        for (int i =0; i<amountOfOutputLayers; i++){
            outputLayersNames.add(namesOfLayers.get(outputLayersIndices.toList().get(i)-1));
        }

        launch(args);
    }

    @FXML
    public void startCamera(ActionEvent actionEvent) {
        if (!cameraActive) {
            cameraActive = true;
            cameraButton.setText("Stop Camera");

            countedObjectIds = new HashSet<>(); // Для хранения идентификаторов объектов
            objectTypeCount = new HashMap<>(); // Для хранения количества распознанных объектов каждого типа
            Services.formObjectTypeCounter(labels, objectTypeCount);
            objectDetector.trackingCounter = 0;

            objectDetector.setNewObjectCallback(this::handleNewObject);

            capture.open(0); //Открываем камеру для захвата изображения
            if (!capture.isOpened()) {
                System.err.println("Не удалось открыть камеру");
                cameraActive = false;
                cameraButton.setText("Start Camera");
                return;
            }

            objectDetector.startDetection(capture, this::onFrameReady, objectTypeCount, countedObjectIds);
        } else {
            cameraActive = false; //Устанавливаем флаг состояния камеры в положение "неактивна"
            cameraButton.setText("Start Camera");
            objectDetector.stopDetection();
            capture.release();
        }
    }

    private void handleNewObject(TrackedObject trackedObject) {
        boolean isNew = detectedObjects.add(trackedObject);
        if (!isNew) return;

        double x = trackedObject.getCenter().x;
        double y = trackedObject.getCenter().y;

        int col = (x < ObjectDetector.CADR_WIDTH / 3.0) ? 0 : (x < 2 * ObjectDetector.CADR_WIDTH / 3.0) ? 1 : 2;
        int row = (y < ObjectDetector.CADR_HEIGHT / 3.0) ? 0 : (y < 2 * ObjectDetector.CADR_HEIGHT / 3.0) ? 1 : 2;

        String position = getPositionDescription(row, col);

        String message = position + " " + trackedObject.getClassName();

        System.out.println("📍 " + message);

    }

    private void onFrameReady(Mat frame) {
        Platform.runLater(()->{
            if (cameraActive){
                imageView.setImage(SwingFXUtils.toFXImage(ObjectDetector.matToBufferedImage(frame), null));
            }
            frame.release();
        });
    }

    private String getPositionDescription(int row, int col) {
        switch (row) {
            case 0: // верхний ряд
                switch (col) {
                    case 0: return "слева сверху";
                    case 1: return "прямо сверху";
                    case 2: return "справа сверху";
                }
            case 1: // средний ряд
                switch (col) {
                    case 0: return "слева";
                    case 1: return "по центру";
                    case 2: return "справа";
                }
            case 2: // нижний ряд
                switch (col) {
                    case 0: return "слева снизу";
                    case 1: return "прямо снизу";
                    case 2: return "справа снизу";
                }
            default:
                return "неизвестно";
        }
    }
}