/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umbertopalazzini.s3zilla.controller;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.util.CollectionUtils;
import com.umbertopalazzini.s3zilla.concurrency.TransferTask;
import com.umbertopalazzini.s3zilla.model.S3ObjectWrapper;
import com.umbertopalazzini.s3zilla.model.S3ObjectWrapperTreeItem;
import com.umbertopalazzini.s3zilla.service.S3Service;
import com.umbertopalazzini.s3zilla.utility.SizeConverter;
import com.umbertopalazzini.s3zilla.view.LogItem;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 *
 * @author sascha
 */
public class ContentController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ContentController.class.getName());

    @FXML
    private TreeTableView<S3ObjectWrapper> objectTree;
    @FXML
    private TreeTableColumn<S3ObjectWrapper, String> objectTreeName;
    @FXML
    private TreeTableColumn<S3ObjectWrapper, Date> objectTreeLastmodified;
    @FXML
    private TreeTableColumn<S3ObjectWrapper, String> objectTreeSize;
    @FXML
    private ContextMenu objectTreeContextMenu;
    @FXML
    private MenuItem contextMenuDelete;
    @FXML
    private MenuItem contextMenuDownload;
    @FXML
    private MenuItem contextMenuNewPrefix;

    @FXML
    private TableView<LogItem> logTable;
    @FXML
    private TableColumn<LogItem, String> logTable_localFile;
    @FXML
    private TableColumn<LogItem, String> logTable_remoteFile;
    @FXML
    private TableColumn<LogItem, ProgressBar> logTable_progress;
    @FXML
    private TableColumn<LogItem, String> logTable_size;
    @FXML
    private TableColumn<LogItem, Label> logTable_status;
    @FXML
    private TableColumn<LogItem, HBox> logTable_actions;

    private S3Service s3Service;
    private Bucket selecetedBucket;
    private MainController mainController;
    private DirectoryChooser directoryChooser;
    private FileChooser fileChooser;
    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        objectTree.setContextMenu(null);

        objectTreeName.setCellValueFactory(column -> new SimpleStringProperty(column.getValue().getValue().getName()));
        // Sets the cell factory for the size column of the filesTable.
        objectTreeLastmodified.setCellValueFactory(column -> new SimpleObjectProperty<>(column.getValue().getValue().getLastModified()));
        // Sets the cell factory for the last modified column of the filesTable.
        objectTreeSize.setCellValueFactory(column -> new SimpleStringProperty(SizeConverter.format(column.getValue().getValue().getSize())));

        directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(resources.getString("directorychooser.title"));

        fileChooser = new FileChooser();
        fileChooser.setTitle(resources.getString("filechooser.title"));

        // Sets the cell factory for the localFile column of the logTable.
        logTable_localFile.setCellValueFactory(column -> new SimpleStringProperty(column.getValue().getLocalFile()));

        // Sets the cell factory for the remoteFile column of the logTable.
        logTable_remoteFile.setCellValueFactory(column -> new SimpleStringProperty(column.getValue().getRemoteFile()));

        // Sets the cell factory for the progress column of the logTable.
        logTable_progress.setCellValueFactory(column -> new SimpleObjectProperty<>(column.getValue().getProgress()));

        // Sets the cell factory for the size column of the logTable.
        logTable_size.setCellValueFactory(column -> new SimpleStringProperty(SizeConverter.format(column.getValue().getSize())));

        // Sets the cell factory for the status column of the logTable.
        logTable_status.setCellValueFactory(column -> new SimpleObjectProperty<>(column.getValue().getStatus()));

        // Sets the cell factory for the actions column of the logTable.
        logTable_actions.setCellValueFactory(column -> new SimpleObjectProperty<>(column.getValue().getActions()));
    }

    public void downloadPressed(ActionEvent actionEvent) {
        logger.debug("downloadPressed: '{}'", actionEvent);
        TreeItem<S3ObjectWrapper> selectedTreeItem = objectTree.getSelectionModel().getSelectedItem();
        ProgressBar progressBar = new ProgressBar(0.0f);
        Label status = new Label();
        HBox actions = new HBox();

        File downloadFolder = directoryChooser.showDialog(mainController.getPrimaryStage());
        TransferManager transferManager = s3Service.getTransferManager();

        if ((null != downloadFolder) && (null != transferManager) && (null != selectedTreeItem)) {
            Transfer download;
            File downloadDestination;
            if (selectedTreeItem.isLeaf()) {
                logger.debug("downloading file '{}'", selectedTreeItem.getValue().getKey());
                downloadDestination = new File(downloadFolder, selectedTreeItem.getValue().getName());
                download = transferManager.download(selectedTreeItem.getValue().getBucketName(), selectedTreeItem.getValue().getKey(), downloadDestination);
            } else {
                logger.debug("downloading dir '{}'", selectedTreeItem.getValue().getKey());
                downloadDestination = downloadFolder;
                download = transferManager.downloadDirectory(selectedTreeItem.getValue().getBucketName(), selectedTreeItem.getValue().getKey(), downloadDestination);
            }
            TransferTask downloadTask = new TransferTask(download, resources, progressBar, status, actions, null);
            logTable.getItems().add(new LogItem(selectedTreeItem.getValue().getName(), progressBar, download, status, actions));
            new Thread(downloadTask).start();
        }
    }

    private UpdateTreeCallback createUploadCallback(final S3ObjectWrapperTreeItem treeItem) {
        return new UpdateTreeCallback() {
            @Override
            public void updateLeafs() {
                logger.debug("updateLeafs: '{}'", treeItem);
                if (treeItem.isLoaded()) {
                    logger.debug("treeItem is loaded .. updating: '{}'", treeItem);
                    ObservableList<S3ObjectWrapperTreeItem> treeItems = buildChildren(treeItem.getValue());
                    treeItem.getChildren().removeAll(treeItem.getChildren().stream().filter(wrapper -> wrapper.isLeaf()).collect(Collectors.toList()));
                    treeItem.getChildren().addAll(treeItems.stream().filter(wrapper -> wrapper.isLeaf()).collect(Collectors.toList()));
                }
            }

            @Override
            public void updateTreeItems() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    public void deletePressed(ActionEvent actionEvent) {
        S3ObjectWrapperTreeItem selectedTreeItem = getSelectTreeItem();
        if (null != selectedTreeItem) {
            String message;
            if (selectedTreeItem.isLeaf()) {
                message = resources.getString("contentcontroller.confirm.delete.content.single").replace("{}", selectedTreeItem.getValue().getKey());
            } else {
                message = resources.getString("contentcontroller.confirm.delete.content.multiple").replace("{}", selectedTreeItem.getValue().getKey());
            }
            Optional<ButtonType> result = mainController.showAndWaitAlert(AlertType.CONFIRMATION, resources.getString("contentcontroller.confirm.delete.title"), message, null);
            if (result.isPresent() && result.get().equals(ButtonType.OK)) {
                try {
                    if (selectedTreeItem.isLeaf()) {
                        s3Service.deleteObject(selectedTreeItem.getValue().getS3ObjectSummary());
                    } else {
                        s3Service.deleteObjects(selectedTreeItem.getValue().getS3ObjectSummary());
                    }
                    removeAndCleanTreeItem(selectedTreeItem);
                } catch (RuntimeException ex) {
                    logger.warn("could not delete files", ex);
                    mainController.showAndWaitAlert(Alert.AlertType.ERROR, resources.getString("s3client.delete.title"), resources.getString("s3client.delete.content"), ex);
                }
            }
        }
    }
    
    private void removeAndCleanTreeItem(S3ObjectWrapperTreeItem selectedTreeItem) {
        logger.debug("removeAndCleanTreeItem: '{}", selectedTreeItem);
        S3ObjectWrapperTreeItem parent = (S3ObjectWrapperTreeItem) selectedTreeItem.getParent();
        if (null != parent) {
            logger.debug("cleaning parent's children: '{}", parent);
            parent.getChildren().remove(selectedTreeItem);
            if (parent.getChildren().isEmpty()) {
                logger.debug("cleaning parent: '{}", parent);
                removeAndCleanTreeItem(parent);
            }
        }
    }

    public void refreshPressed(ActionEvent actionEvent) {
        logger.debug("refreshPressed: '{}'", actionEvent);
        S3ObjectWrapperTreeItem selecteItem = getSelectNonLeafTreeItemOrRootItem();
        if (selecteItem.isLoaded()) {
            logger.debug("reloading treeitem '{}'", selecteItem);
            selecteItem.getChildren().clear();
            selecteItem.getChildren().addAll(buildChildren(selecteItem.getValue()));
        }
        logger.debug("refresh done");
    }
    
    public void newPrefixPressed(ActionEvent actionEvent) {
        logger.debug("newPrefixPressed: '{}'", actionEvent);
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(resources.getString("contentcontroller.newfolderdialog.title"));
        dialog.setHeaderText(null);
        dialog.setGraphic(null);
        dialog.setContentText(resources.getString("contentcontroller.newfolderdialog.content"));
        dialog.getEditor().setPromptText(resources.getString("contentcontroller.newfolderdialog.prompttext"));

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            S3ObjectWrapperTreeItem selectedTreeItem = getSelectNonLeafTreeItemOrRootItem();
            if (!selectedTreeItem.isLoaded()) {
                loadAndAddChildren(selectedTreeItem);
            }
            S3ObjectSummary sum = new S3ObjectSummary();
            sum.setBucketName(selectedTreeItem.getValue().getBucketName());
            if (objectTree.getRoot() == selectedTreeItem) {
                sum.setKey(result.get() + "/");
            } else {
                sum.setKey(selectedTreeItem.getValue().getKey() + result.get() + "/");
            }
            S3ObjectWrapperTreeItem newItem = new S3ObjectWrapperTreeItem(false, true, new S3ObjectWrapper(sum));
            int pos = 0;
            for (; pos < selectedTreeItem.getChildren().size(); pos++) {
                if (selectedTreeItem.getChildren().get(pos).isLeaf()) {
                    break;
                }
                if (selectedTreeItem.getChildren().get(pos).getValue().getName().compareTo(result.get()) > 0) {
                    break;
                }
            }
            selectedTreeItem.getChildren().add(pos, newItem);
        }
    }

    public void uploadPressed(ActionEvent actionEvent) {
        logger.debug("uploadPressed: '{}'", actionEvent);
        ProgressBar progressBar = new ProgressBar(0.0f);
        Label status = new Label();
        HBox actions = new HBox();

        File uploadFile = fileChooser.showOpenDialog(mainController.getPrimaryStage());
        TransferManager transferManager = s3Service.getTransferManager();
        S3ObjectWrapperTreeItem selectedTreeItem = getSelectNonLeafTreeItemOrRootItem();

        if ((null != uploadFile) && (null != transferManager) && (null != selecetedBucket)) {
            String key;
            if (selectedTreeItem == objectTree.getRoot()) {
                key = uploadFile.getName();
            } else {
                key = selectedTreeItem.getValue().getKey() + uploadFile.getName();
            }
            Upload upload = transferManager.upload(selecetedBucket.getName(), key, uploadFile);

            TransferTask uploadTask = new TransferTask(upload, resources, progressBar, status, actions, createUploadCallback(selectedTreeItem));
            logTable.getItems().add(new LogItem(key, progressBar, upload, status, actions));

            new Thread(uploadTask).start();
        }
    }
    
    private S3ObjectWrapperTreeItem getSelectNonLeafTreeItemOrRootItem() {
        S3ObjectWrapperTreeItem result = getSelectTreeItemOrRootItem();
        if (result.isLeaf()) {
            result = (S3ObjectWrapperTreeItem) result.getParent();
        }
        return result;
    }
    
    private S3ObjectWrapperTreeItem getSelectTreeItemOrRootItem() {
        S3ObjectWrapperTreeItem result = (S3ObjectWrapperTreeItem) objectTree.getSelectionModel().getSelectedItem();
        if (result == null) {
            result = (S3ObjectWrapperTreeItem) objectTree.getRoot();
        }
        return result;
    }
    
    private S3ObjectWrapperTreeItem getSelectTreeItem() {
        return (S3ObjectWrapperTreeItem) objectTree.getSelectionModel().getSelectedItem();
    }

    public void contextMenuShowing(WindowEvent windowEvent) {
        logger.debug("contextMenuShowing: '{}'", windowEvent);
        contextMenuDelete.setDisable(objectTree.getSelectionModel().getSelectedItem() == null);
        contextMenuDownload.setDisable(objectTree.getSelectionModel().getSelectedItem() == null);
    }

    private S3ObjectWrapperTreeItem buildBucketTree(Bucket bucket) {
        try {
            ListObjectsV2Result result = s3Service.listObjects(bucket.getName(), null);
            S3ObjectSummary sum = new S3ObjectSummary();
            //sum.setKey(bucket.getName());
            sum.setBucketName(bucket.getName());
            S3ObjectWrapperTreeItem root = new S3ObjectWrapperTreeItem(false, true, new S3ObjectWrapper(sum));
            root.getChildren().setAll(buildChildren(result));
            return root;
        } catch (SdkClientException ex) {
            logger.warn("could not list files", ex);
            mainController.showAndWaitAlert(AlertType.ERROR, resources.getString("s3client.buildbuckettree.title"), resources.getString("s3client.buildbuckettree.content"), ex);
        }
        return null;
    }

    public ObservableList<S3ObjectWrapperTreeItem> buildChildren(S3ObjectWrapper wrapper) {
        try {
            ListObjectsV2Result result = s3Service.listObjects(wrapper.getS3ObjectSummary().getBucketName(), wrapper.getKey());
            return buildChildren(result);
        } catch (SdkClientException ex) {
            logger.warn("could not list files", ex);
            mainController.showAndWaitAlert(Alert.AlertType.ERROR, resources.getString("s3client.buildchildren.title"), resources.getString("s3client.buildchildren.content"), ex);
        }
        return null;
    }

    private ObservableList<S3ObjectWrapperTreeItem> buildChildren(ListObjectsV2Result result) {
        logger.debug("buildChildren: '{}'", result);
        if ((null != result) && ((!CollectionUtils.isNullOrEmpty(result.getCommonPrefixes())) || (!CollectionUtils.isNullOrEmpty(result.getObjectSummaries())))) {
            ObservableList<S3ObjectWrapperTreeItem> children = FXCollections.observableArrayList();
            if (null != result.getCommonPrefixes()) {
                children.addAll(result.getCommonPrefixes().stream().map(prefix -> {
                    S3ObjectSummary sum = new S3ObjectSummary();
                    sum.setKey(prefix);
                    sum.setBucketName(result.getBucketName());
                    return new S3ObjectWrapperTreeItem(false, new S3ObjectWrapper(sum));
                }).collect(Collectors.toList()));
            }
            if (null != result.getObjectSummaries()) {
                children.addAll(result.getObjectSummaries().stream().map(this::createObjectItem).collect(Collectors.toList()));
            }
            logger.debug("returning children: '{}'", children);
            return children;
        }
        logger.debug("returning children: 'empty'");
        return FXCollections.emptyObservableList();
    }

    private S3ObjectWrapperTreeItem createObjectItem(S3ObjectSummary value) {
        return new S3ObjectWrapperTreeItem(true, new S3ObjectWrapper(value));
    }

    public void setS3Service(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setSelecetedBucket(Bucket selecetedBucket) {
        this.selecetedBucket = selecetedBucket;
        if (null == selecetedBucket) {
            objectTree.setRoot(null);
            objectTree.setContextMenu(null);
        } else {
            S3ObjectWrapperTreeItem rootNode = buildBucketTree(selecetedBucket);
            if (null != rootNode) {
                rootNode.addEventHandler(TreeItem.branchExpandedEvent(), treeEventhandler());
            }
            objectTree.setRoot(rootNode);
            objectTree.setContextMenu(objectTreeContextMenu);
        }
    }

    private EventHandler<TreeItem.TreeModificationEvent<S3ObjectWrapper>> treeEventhandler() {
        return (TreeItem.TreeModificationEvent<S3ObjectWrapper> event) -> {
            S3ObjectWrapperTreeItem source = (S3ObjectWrapperTreeItem) event.getSource();
            if ((!source.isLeaf()) && (!source.isLoaded())) {
                loadAndAddChildren(source);
            }
        };
    }
    
    private void loadAndAddChildren(S3ObjectWrapperTreeItem treeItem) {
        treeItem.getChildren().setAll(buildChildren(treeItem.getValue()));
        treeItem.setLoaded(true);
    }
}
