package SpellChecker;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public class Main extends Application {

    public static final HashSet<String> dictionary = new HashSet<String>();
    private ObservableList misspelledWords = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Spell Checker");

        final FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        final Button addToDictionaryBtn = new Button("Add to Dictionary");
        final Button loadTextBtn = new Button("Load Text");

        final Label inputTextLabel = new Label("Input Text");
        final TextArea inputText = new TextArea();
        final Button spellCheckBtn = new Button("Spell Check");

        final Label listViewLabel = new Label("Misspelled Words");
        final ListView wordsListView = new ListView(misspelledWords);
        final Button addSuggBtn = new Button("Add Suggestion");
        final Button saveSuggBtn = new Button("Save Suggestions");
        wordsListView.setEditable(true);

        HashMap<String, String> suggestions = new HashMap<String, String>();

        addToDictionaryBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                File file = fileChooser.showOpenDialog(stage);
                if(file != null){
                    try (final Stream<String> lines = Files.lines(file.toPath())) {
                        dictionary.addAll(lines
                                .map(line -> line.split("[\\s]+"))
                                .flatMap(Arrays::stream)
                                .distinct()
                                .collect(Collectors.toList()));
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        loadTextBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                File file = fileChooser.showOpenDialog(stage);
                if(file != null){
                    try {
                        String content = new String(Files.readAllBytes(file.toPath()));
                        inputText.setText(content);
                        suggestions.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        spellCheckBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try (final Stream<String> lines = Arrays.stream(inputText.getText().split("\\W+"))) {
                    misspelledWords = lines
                            .parallel()
                            .map(line -> line.split("[\\s]+"))
                            .flatMap(Arrays::stream)
                            .distinct()
                            .filter(s -> !dictionary.contains(s))
                            .collect(toCollection(FXCollections::observableArrayList));
                    wordsListView.setItems(misspelledWords);
                }
                suggestions.clear();
            }
        });

        addSuggBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String selectedWord = (String) wordsListView.getSelectionModel().getSelectedItem();
                if(selectedWord != null) {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Add Suggestion");
                    dialog.setContentText("Please enter a suggestion for " + selectedWord + ":");

                    // Traditional way to get the response value.
                    Optional<String> result = dialog.showAndWait();
                    if (result.isPresent()) {
                        suggestions.put(selectedWord, result.get());
                    }
                }
            }
        });

        saveSuggBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String text = inputText.getText();
                for(String s : suggestions.keySet()){
                    String regex = "\\b" + Pattern.quote(s) + "\\b";
                    text = text.replaceAll(regex, suggestions.get(s));
                    dictionary.add(suggestions.get(s));
                    misspelledWords.remove(s);
                }
                inputText.setText(text);
                suggestions.clear();
            }
        });

        BorderPane border = new BorderPane();

        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 15, 15, 15));
        hbox.setSpacing(10);
        hbox.getChildren().addAll(addToDictionaryBtn, loadTextBtn);
        border.setTop(hbox);

        VBox leftPane = new VBox();
        leftPane.setPadding(new Insets(15, 15, 15, 15));
        leftPane.setSpacing(10);
        leftPane.getChildren().addAll(inputTextLabel, inputText, spellCheckBtn);
        border.setLeft(leftPane);

        VBox rightPane = new VBox();
        rightPane.setPadding(new Insets(15, 15, 15, 15));
        rightPane.setSpacing(10);
        rightPane.getChildren().addAll(listViewLabel, wordsListView, addSuggBtn, saveSuggBtn);
        border.setRight(rightPane);

        stage.setScene(new Scene(border, 800, 300));
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
