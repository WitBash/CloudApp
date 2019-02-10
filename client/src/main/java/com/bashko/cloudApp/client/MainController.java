package com.bashko.cloudApp.client;

import com.bashko.cloudApp.common.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Function;

public class MainController implements Initializable {

    @FXML
    ListView<String> filesList;

    @FXML
    ListView<String> filesListServer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get("client_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
                    }
                    if (am instanceof FileListUpdate) {
                        FileListUpdate flu = (FileListUpdate) am;
                        Platform.runLater(() -> {
                            filesListServer.getItems().clear();
                            flu.getList().forEach(o -> filesListServer.getItems().add(o));
                        });
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                System.out.println("Сеть отключена");
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
        refreshLocalFilesList();
        refreshLocalFilesListServer();
    }

    public void refreshLocalFilesListServer() {
        Network.sendMsg(new FileListUpdate(null));
    }


    public String getSelected(ListView<String> listView) {
        String selectFile = listView.getSelectionModel().getSelectedItem();
        listView.getSelectionModel().clearSelection();
        return selectFile;
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        String selectFile = getSelected(filesListServer);
        if (selectFile == null) return;
        Network.sendMsg(new FileRequest(selectFile));
    }

    public void pressOnSendBtn(ActionEvent actionEvent) {
        String selectFile = getSelected(filesList);
        if (selectFile == null) return;
        try {
            Network.sendMsg(new FileMessage(Paths.get("client_storage/" + selectFile)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pressOnDeleteClientFileBtn(ActionEvent actionEvent) {
        String selectFile = getSelected(filesList);
        if (selectFile == null) return;
        try {
            Files.delete(Paths.get("client_storage/" + selectFile));
            refreshLocalFilesList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pressOnDeleteServerFileBtn(ActionEvent actionEvent) {
        String selectFile = getSelected(filesListServer);
        if (selectFile == null) return;
        Network.sendMsg(new FileDelete(selectFile));
    }

    public void refreshLocalFilesList() {
        if (Platform.isFxApplicationThread()) {
            try {
                filesList.getItems().clear();
                Files.list(Paths.get("client_storage")).map(new Function<Path, String>() {
                    @Override
                    public String apply(Path p) {
                        return p.getFileName().toString();
                    }
                }).forEach(o -> {
                    filesList.getItems().add(o);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        filesList.getItems().clear();
                        Files.list(Paths.get("client_storage")).map(new Function<Path, String>() {
                            @Override
                            public String apply(Path p) {
                                return p.getFileName().toString();
                            }
                        }).forEach(new Consumer<String>() {
                            @Override
                            public void accept(String o) {
                                filesList.getItems().add(o);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
