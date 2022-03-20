import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
//import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        //task1
        //создание csv файла data.csv
        String[] employee1 = "1,John,Smith,USA,25".split(",");
        String[] employee2 = "2,Ivan,Petrov,RU,23".split(",");
        String csvFileName = "data.csv";
        makeCSVFileFromTwoLine(employee1, employee2, csvFileName);

        //чтение csv файла data.csv
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        List<Employee> listEmployeesOfCsv = parseCSV(columnMapping, csvFileName);

        //создание json файла data.json
        String json = convertListToJson(listEmployeesOfCsv);
        String jsonFileName = "data.json";
        writeToFile(new File(jsonFileName), json);

        //task2
        //создание xml файла data.xml
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element staff = document.createElement("staff");
            document.appendChild(staff);

            Element employee1_1 = document.createElement("employee");
            employee1_1.setAttribute("id", "1");
            employee1_1.setAttribute("firstName", "John");
            employee1_1.setAttribute("lastName", "Smith");
            employee1_1.setAttribute("country", "USA");
            employee1_1.setAttribute("age", "25");
            staff.appendChild(employee1_1);

            Element employee2_2 = document.createElement("employee");
            employee2_2.setAttribute("id", "2");
            employee2_2.setAttribute("firstName", "Ivan");
            employee2_2.setAttribute("lastName", "Petrov");
            employee2_2.setAttribute("country", "RU");
            employee2_2.setAttribute("age", "23");
            staff.appendChild(employee2_2);

            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File("data.xml"));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(domSource, streamResult);
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }

        //парсинг xml файла data.xml
        List<Employee> listEmployeesOfXml = parseXML("data.xml");
        String json2 = convertListToJson(listEmployeesOfXml);

        //создание json файла data2.json
        String jsonFileName2 = "data2.json";
        writeToFile(new File(jsonFileName2), json2);

        //task3
        //чтение json файла data.json
        String newJsonFromData = readString("data.json");
        List<Employee> listForJson = convertJsonToList(newJsonFromData);
        listForJson.forEach(System.out::println);
    }

    public static void makeCSVFileFromTwoLine(String[] line1, String[] line2, String fileName) {
        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(fileName, false))) {
            csvWriter.writeNext(line1, false);
            csvWriter.writeNext(line2, false);
            csvWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> list = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy =
                    new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);

            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();

            list = csv.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static <T> String convertListToJson(List<T> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(list);
    }

    public static void writeToFile(File fileName, String text) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(text);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Employee> parseXML(String fileName) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(fileName));

            Node root = document.getDocumentElement();
            return read(root);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List<Employee> read(Node node) {
        List<Employee> list = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node nodeChild = nodeList.item(i);
            if (Node.ELEMENT_NODE == nodeChild.getNodeType()) {
                Element element = (Element) nodeChild;
                NamedNodeMap map = element.getAttributes();
                if (element.getNodeName().equals("employee")) {
                    try {
                        Employee employee =  new Employee();
                        for (int j = 0; j < map.getLength(); j++) {
                            if (map.item(j).getNodeName().equals("id")) {
                                employee.id = Long.parseLong(map.item(j).getNodeValue());
                            }
                            if (map.item(j).getNodeName().equals("firstName")) {
                                employee.firstName = map.item(j).getNodeValue();
                            }
                            if (map.item(j).getNodeName().equals("lastName")) {
                                employee.lastName = map.item(j).getNodeValue();
                            }
                            if (map.item(j).getNodeName().equals("country")) {
                                employee.country = map.item(j).getNodeValue();
                            }
                            if (map.item(j).getNodeName().equals("age")) {
                                employee.age = Integer.parseInt(map.item(j).getNodeValue());
                            }
                        }
                        list.add(employee);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    read(nodeChild);
                }
            }
        }
        return list;
    }

    public static String readString(String fileName) {
        String s;
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            while ((s = reader.readLine()) != null) {
                builder.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public static List<Employee> convertJsonToList(String json) {

        //первый вариант
        /*GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        List<Employee> employees = gson.fromJson(json, new TypeToken<List<Employee>>() {}.getType());*/

        //вариант по тз
        List<Employee> employees = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            Object object = parser.parse(json);
            JSONArray jsonArray = (JSONArray) object;
            for (Object employee : jsonArray) {
                JSONObject jsonObject1 = (JSONObject) employee;
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                Employee employee1 = gson.fromJson(String.valueOf(jsonObject1), Employee.class);
                employees.add(employee1);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return employees;
    }
}
