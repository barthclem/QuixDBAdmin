import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.sql.*;

public class Controller {
    @FXML
    private ComboBox<String> topicsCombo;
    @FXML
    private AnchorPane newTopicPane;
    @FXML
    private TextField topicField;
    @FXML
    private Label addTopicFeedback;
    @FXML
    private TextField sessionField;
    @FXML
    private Label addSessionFeedback;
    @FXML
    private TextArea sessionList;
    @FXML
    private ComboBox<String > sessions;
    @FXML
    private TextArea questionField;
    @FXML
    private TextField optionField;
    @FXML
    private TextField answerField;
    @FXML
    private Label addEntryFeedback;


    private Connection connection;
    private ResultSet topics;
    private DatabaseMetaData dbmetaData;
    private String TABLE,TABLESESSION,result;
    private Statement stat;
    private ObservableList<String> resultList;
    private int sessionId;
    @FXML
    private void initialize(){
        try {
            connection=Connections.getConnection();
            stat=connection.createStatement();
            loadTopics();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void createTopic(){
        newTopicPane.setOpacity(1);
        newTopicPane.setDisable(false);
    }
    private void loadTopics() throws SQLException {
        dbmetaData=connection.getMetaData();
        topics=dbmetaData.getTables("","","",new String[]{"TABLE"});
        ObservableList<String> topicslist= FXCollections.observableArrayList();
        while(topics.next()){
            String name=topics.getString("TABLE_NAME");
            System.out.println("name -> "+name);
            if(!name.matches(".*Sessions.*")){
                System.out.println("Entry name -> "+name);
                topicslist.add(name);}
        }
        Platform.runLater(()->{topicsCombo.setItems(topicslist);});
    }
    @FXML
    private void addTopic(){
        TABLE=topicField.getText().trim()+"Quiz";
        TABLESESSION=TABLE+"Sessions";
        String sessionSql="create table "+TABLESESSION+"(" +
                "id integer auto_increment," +
                "name varchar(255)," +
                "primary key(id)" +
                ");" ;

        String sql=  "create table "+TABLE+"(" +
                "_id integer auto_increment," +
                "questions varchar(255)," +
                "option1 varchar(255)," +
                "option2 varchar(255)," +
                "option3 varchar(255)," +
                "option4 varchar(255)," +
                "answer varchar(255)," +
                "time varchar(255)," +
                "section integer," +
                "primary key(_id,section)," +
                "foreign key(section) references "+TABLESESSION+"(id)" +
                ");";
        try {
            boolean know=stat.execute(sessionSql);
            boolean status=stat.execute(sql);
            if(status&&know){
                addTopicFeedback.setText("Adding Failed");
            }
            else{
                addTopicFeedback.setText("Added Successfully");
                loadTopics();
                loadSessions(TABLE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    @FXML
    private void selectTopic(){
        TABLE=topicsCombo.getValue();
        TABLESESSION=TABLE+"Sessions";
        try {
            System.out.println("Chosen Table is : "+TABLE);
            loadSessions(TABLESESSION);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addSection(){
        String newSection=sessionField.getText().trim();
        String sql="insert into "+TABLESESSION+"(name) values('"+newSection+"');";
        try {

            int status=stat.executeUpdate(sql);
            if(status>0){
                addSessionFeedback.setText("Added Successfully");
                result+="\t"+newSection;
                sessionList.setText(result);
            }
            else{
                addSessionFeedback.setText("Adding Failed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void loadSessions(String table) throws SQLException {
        String sql="select * from "+table;
        Statement stat=connection.createStatement();
        ResultSet sessionresult=stat.executeQuery(sql);
        result=""; String holder="";
        resultList=FXCollections.observableArrayList();
        while(sessionresult.next()){
            holder=sessionresult.getString("name");
            resultList.add(holder);
            result+=holder+"\t";
        }
        sessions.setItems(resultList);
        sessionList.setText(result);
    }

    @FXML
    private void selectSession()
    {
        String session=sessions.getValue();
        sessionId=resultList.indexOf(session)+1;
        System.out.println("The index of the chosen session is : "+sessionId);
        questionField.setText("");
        optionField.setText("");
        answerField.setText("");
    }

    @FXML
    private void addNewEntry(){
        String question=questionField.getText().trim();
        String options=optionField.getText().trim();
        String answer=answerField.getText().trim();
        try {
            insertEntry(sessionId,question,options,answer);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertEntry(Integer section,String question,String options,String answer) throws SQLException {
        String optionA="",optionB="",optionC="",optionD="";        //Get All the available Strings
        if(!options.isEmpty()){
            String[] option=options.split(",");
            optionA=option[0].trim();
            if(option.length>=1)
                optionB=option[1].trim();
            if(option.length>=2)
                optionC=option[2].trim();
            if(option.length>=3)
                optionD=option[3].trim();

        }

        boolean insert=stat.execute(
                "insert into " +TABLE+
                        "(section,questions,option1,option2,option3,option4,answer)" +
                        "values("+section+",'"+ question+"','"+optionA+"','"+optionB+"','"+optionC+"','"+optionD+"','"+answer+"')"

        );

        if(insert) {
            addEntryFeedback.setText("Adding Failed");


        }
        else {
            addEntryFeedback.setText("Added Successfully");
            questionField.setText("");
            optionField.setText("");
            answerField.setText("");

        }

    }

}
