
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Node {
    private String type;
    private List<Node> children;

    public Node(String type) {
        this.type = type;
        this.children = new ArrayList<>();
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public String getType() {
        return type;
    }

    public List<Node> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(type);
        if (!children.isEmpty()) {
            builder.append(": ");
            for (Node child : children) {
                builder.append(child.toString()).append(" ");
            }
        }
        return builder.toString();
    }
}

class Parser {
    private List<Lexeme> tokens;
    private int currentTokenIndex;
    public Map<String, String> pointCoordinates; // Memory for storing coordinates
    private String graphicCode = "import javax.swing.*;\n" +
            "import java.util.ArrayList;\n"+
            "public class Code {\n" +
            "    public static void main(String[] args) {" +
            "       JFrame frame = new JFrame(\"Geometry Drawer\");\n" +
            "       ArrayList<Geometry> geometries = new ArrayList<>();\n";


    public String generateCode(){
        return graphicCode += "       DrawingPanel drawingPanel = new DrawingPanel(geometries);\n" +
                "       frame.add(drawingPanel);\n" +
                "       frame.setSize(800, 600);\n" +
                "       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);\n" +
                "       frame.setLocationRelativeTo(null);\n" +
                "       frame.setResizable(false);\n" +
                "       frame.setVisible(true);\n" +
                "    }\n" +
                "}\n";
    }

    public void saveCodeToFile() {
        try (FileWriter writer = new FileWriter("src/Code.java")) {
            writer.write(generateCode());
        } catch (IOException e) {
            System.out.println("Error writing code to file: " + e.getMessage());
        }
    }




    public Parser(List<Lexeme> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
        this.pointCoordinates = new HashMap<>();
    }

    private Lexeme currentToken() {
        if (currentTokenIndex < tokens.size()) {
            return tokens.get(currentTokenIndex);
        }
        return null;
    }

    private void consume(String expectedType) {
        if (currentTokenIndex < tokens.size() && currentToken().getType().equals(expectedType)) {
            currentTokenIndex++;
        } else {
            throw new RuntimeException("Unexpected token: " + (currentToken() != null ? currentToken().getLexeme() : "EOF") + ", expected: " + expectedType);
        }
    }

    private Lexeme consumeAndGet(String expectedType) {
        if (currentTokenIndex < tokens.size() && currentToken().getType().equals(expectedType)) {
            return tokens.get(currentTokenIndex++);
        } else {
            throw new RuntimeException("Expected " + expectedType + " but found: " + (currentToken() != null ? currentToken().getLexeme() : "EOF"));
        }
    }

    private void printTree(Node node, String indent, boolean last) {
        String symbol = last ? "└── " : "├── ";
        System.out.println(indent + symbol + node.getType());

        for (int i = 0; i < node.getChildren().size(); i++) {
            printTree(node.getChildren().get(i), indent + (last ? "    " : "│   "), i == node.getChildren().size() - 1);
        }
    }

    public Node parse() {
        Node programNode = new Node("Program");
        Node textNode = parseText();
        programNode.addChild(textNode);

        printTree(programNode, "", true);
        return programNode;
    }

    private Node parseText() {
        Node textNode = new Node("Operation");
        Node operationNode = parseOperation();
        textNode.addChild(operationNode);

        if (currentTokenIndex < tokens.size() && currentToken().getLexeme().equals(";")) {
            consume("DELIMITER"); // consume ';'
            if (currentTokenIndex < tokens.size()) {
                textNode.addChild(parseText());
            }
        }

        return textNode;
    }


    private Node parseOperation() {
        if (currentTokenIndex < tokens.size() && currentToken().getType().equals("RESERVED_WORD")) {
            String operation = currentToken().getLexeme();
            if (operation.startsWith("поставити")) {
                return handlePutPoint();
            } else if (operation.startsWith("зʼєднати")) {
                return handleConnectPoints();
            } else if (operation.startsWith("провести")) {
                consume("RESERVED_WORD"); // consume "провести"
                if (currentToken().getLexeme().equals("пряму")) {
                    return handleDrawPerpendicular();
                } else {
                    return handleDrawSegment();
                }
            } else if (operation.startsWith("побудувати")) {
                consume("RESERVED_WORD"); // "побудувати"
                if (currentToken().getLexeme().equals("трикутник")) {
                    return handleBuildTriangle();
                } else if (currentToken().getLexeme().equals("квадрат")) {
                    return handleBuildSquare();
                }
            }
        }
        throw new RuntimeException("Unknown operation: " + currentToken().getLexeme());
    }


    public ArrayList<Geometry> geometries = new ArrayList<>();

    private Node handlePutPoint() {
        consume("RESERVED_WORD"); // "поставити"
        consume("GEOMETRY"); // "точку"
        String pointName = consumeAndGet("NAME").getLexeme(); // <назва>

        String coordinates = getCoords(pointName);

        // Store the point name and coordinates in memory
        pointCoordinates.put(pointName, coordinates);

        Node putPointNode = new Node("PutPoint");
        putPointNode.addChild(new Node(pointName));
        putPointNode.addChild(new Node(coordinates));

        String[] coords = coordinates.replaceAll("[()]", "").split(",");
        int x = Integer.parseInt(coords[0].trim());
        int y = Integer.parseInt(coords[1].trim());
        geometries.add(new PointGeometry(pointName, x, y));

        graphicCode += "       geometries.add(new PointGeometry(\""+pointName+"\","+ x+","+ y+"));\n";

        return putPointNode;
    }

    private String generateUniqueCoordinates() {
        int x, y;
        String coordinates;
        do {
            x = (int) (Math.random() * 4);
            y = (int) (Math.random() * 6);
            coordinates = "(" + x + "," + y + ")";
        } while (pointCoordinates.containsValue(coordinates));
        return coordinates;
    }

    private Node handleConnectPoints() {
        consume("RESERVED_WORD"); // "зʼєднати"
        consume("GEOMETRY"); // "точки"

        Node listOfPointsNode = parseListOfPoints(); // <список точок>
        Node connectPointsNode = new Node("ConnectPoints");

        List<String> pointNames = new ArrayList<>();
        List<String> pointCoordinatesList = new ArrayList<>();

        // First pass: Store point names and coordinates
        for (Node pointNode : listOfPointsNode.getChildren()) {
            String[] parts = pointNode.toString().split(" ");
            String pointName = parts[0];
            String coordinates = getCoords(pointName);

            // Store names and coordinates for segment drawing
            pointNames.add(pointName);
            pointCoordinatesList.add(coordinates);

            // Create a node for putting the point
            Node putPointNode = new Node("PutPoint");
            putPointNode.addChild(new Node(pointName));
            putPointNode.addChild(new Node(coordinates));
            connectPointsNode.addChild(putPointNode);

            String[] coords = coordinates.replaceAll("[()]", "").split(",");
            int x = Integer.parseInt(coords[0].trim());
            int y = Integer.parseInt(coords[1].trim());

            geometries.add(new PointGeometry(pointName, x, y));
            graphicCode += "       geometries.add(new PointGeometry(\""+pointName+"\","+ x+","+ y+"));\n";

        }

        // Second pass: Draw segments between unique pairs of points
        for (int i = 0; i < pointNames.size(); i++) {
            for (int j = i + 1; j < pointNames.size(); j++) { // Only connect unique pairs
                String pointName1 = pointNames.get(i);
                String pointName2 = pointNames.get(j);

                // Get coordinates for the two points
                String coordinates1 = pointCoordinates.get(pointName1);
                String coordinates2 = pointCoordinates.get(pointName2);

                // Parse coordinates into integers
                String[] coords1 = coordinates1.replaceAll("[()]", "").split(",");
                String[] coords2 = coordinates2.replaceAll("[()]", "").split(",");

                int x1 = Integer.parseInt(coords1[0].trim());
                int y1 = Integer.parseInt(coords1[1].trim());
                int x2 = Integer.parseInt(coords2[0].trim());
                int y2 = Integer.parseInt(coords2[1].trim());

                // Create a node for drawing the segment
                Node drawSegmentNode = new Node("DrawSegment");
                drawSegmentNode.addChild(new Node(pointName1 + " " + coordinates1)); // First point
                drawSegmentNode.addChild(new Node(pointName2 + " " + coordinates2)); // Second point

                // Add the segment geometry
                geometries.add(new SegmentGeometry(x1, y1, x2, y2)); // Add segment geometry
                graphicCode += "       geometries.add(new SegmentGeometry("+x1+","+ y1+","+ x2+","+y2+"));\n";
                connectPointsNode.addChild(drawSegmentNode); // Add to connect points node
            }
        }

        return connectPointsNode;
    }


    private Node handleDrawSegment() {
        consume("GEOMETRY"); // "відрізок"
        consume("RESERVED_WORD"); // "через"
        consume("RESERVED_WORD"); // "дві"
        consume("GEOMETRY"); // "точки"

        String pointA = consumeAndGet("NAME").getLexeme(); // <точка> (пункт A)
        String coordinatesA = getCoords(pointA);

        consume("RESERVED_WORD"); // "та"; move to next token

        String pointB = consumeAndGet("NAME").getLexeme(); // <точка> (пункт B)
        String coordinatesB = getCoords(pointB);

        Node drawSegmentNode = new Node("DrawSegment");
        drawSegmentNode.addChild(new Node(pointA + " " + coordinatesA)); // точка A
        drawSegmentNode.addChild(new Node(pointB + " " + coordinatesB)); // точка B

        // Parse coordinates to integer values
        String[] coordsA = coordinatesA.replaceAll("[()]", "").split(",");
        String[] coordsB = coordinatesB.replaceAll("[()]", "").split(",");
        int xA = Integer.parseInt(coordsA[0].trim());
        int yA = Integer.parseInt(coordsA[1].trim());
        int xB = Integer.parseInt(coordsB[0].trim());
        int yB = Integer.parseInt(coordsB[1].trim());

        // Store the points in the coordinates map
        pointCoordinates.put(pointA, coordinatesA);
        pointCoordinates.put(pointB, coordinatesB);

        // Add the geometries for both points and the segment
        geometries.add(new SegmentGeometry(xA, yA, xB, yB));
        geometries.add(new PointGeometry(pointA, xA, yA));
        geometries.add(new PointGeometry(pointB, xB, yB));

        graphicCode += "       geometries.add(new SegmentGeometry("+xA+","+ yA+","+ xB+","+yB+"));\n";
        graphicCode += "       geometries.add(new PointGeometry(\""+pointA+"\","+ xA+","+ yA+"));\n";
        graphicCode += "       geometries.add(new PointGeometry(\""+pointB+"\","+ xB+","+ yB+"));\n";

        return drawSegmentNode;
    }


    private Node handleBuildTriangle() {
        consume("GEOMETRY"); // "трикутник"
        consume("RESERVED_WORD"); // "за"
        consume("GEOMETRY"); // "точками"

        String pointA = consumeAndGet("NAME").getLexeme(); // <точка>
        String coordinatesA = getCoords(pointA);

        consume("DELIMITER"); // ","

        // Parse the second point
        String pointB = consumeAndGet("NAME").getLexeme(); // <точка>
        String coordinatesB = getCoords(pointB);

        consume("DELIMITER"); // ","

        // Parse the third point
        String pointC = consumeAndGet("NAME").getLexeme(); // <точка>
        String coordinatesC = getCoords(pointC);

        Node buildTriangleNode = new Node("BuildTriangle");
        buildTriangleNode.addChild(new Node("PutPoint "+pointA + " " + coordinatesA));
        buildTriangleNode.addChild(new Node("PutPoint "+pointB + " " + coordinatesB));
        buildTriangleNode.addChild(new Node("PutPoint "+pointC + " " + coordinatesC));


        String[] coordsA = coordinatesA.replaceAll("[()]", "").split(",");
        String[] coordsB = coordinatesB.replaceAll("[()]", "").split(",");
        String[] coordsC = coordinatesC.replaceAll("[()]", "").split(",");

        int xA = Integer.parseInt(coordsA[0].trim());
        int yA = Integer.parseInt(coordsA[1].trim());
        int xB = Integer.parseInt(coordsB[0].trim());
        int yB = Integer.parseInt(coordsB[1].trim());
        int xC = Integer.parseInt(coordsC[0].trim());
        int yC = Integer.parseInt(coordsC[1].trim());

        pointCoordinates.put(pointA, coordinatesA);
        pointCoordinates.put(pointB, coordinatesB);
        pointCoordinates.put(pointC, coordinatesC);


        geometries.add(new SegmentGeometry(xA, yA, xB, yB));
        geometries.add(new SegmentGeometry(xB, yB, xC, yC));
        geometries.add(new SegmentGeometry(xC, yC, xA, yA));

        geometries.add(new PointGeometry(pointA, xA, yA));
        geometries.add(new PointGeometry(pointB, xB, yB));
        geometries.add(new PointGeometry(pointC, xC, yC));


        graphicCode += "       geometries.add(new SegmentGeometry("+xA+","+ yA+","+ xB+","+yB+"));\n";
        graphicCode += "       geometries.add(new SegmentGeometry("+xB+","+ yB+","+ xC+","+yC+"));\n";
        graphicCode += "       geometries.add(new SegmentGeometry("+xC+","+ yC+","+ xA+","+yA+"));\n";

        graphicCode += "       geometries.add(new PointGeometry(\""+pointA+"\","+ xA+","+ yA+"));\n";
        graphicCode += "       geometries.add(new PointGeometry(\""+pointB+"\","+ xB+","+ yB+"));\n";
        graphicCode += "       geometries.add(new PointGeometry(\""+pointC+"\","+ xC+","+ yC+"));\n";



        return buildTriangleNode;
    }

    private String getCoords(String pointC) {
        String coordinatesC;
        if (currentTokenIndex < tokens.size() && currentToken().getType().equals("COORDINATES")) {
            coordinatesC = consumeAndGet("COORDINATES").getLexeme();
            pointCoordinates.put(pointC, coordinatesC);
        } else {
            coordinatesC = pointCoordinates.get(pointC);
            if (coordinatesC == null) {
                coordinatesC = generateUniqueCoordinates();
                pointCoordinates.put(pointC, coordinatesC);
            }
        }
        return coordinatesC;
    }


    private Node handleBuildSquare() {
        consume("GEOMETRY"); // "квадрат"
        consume("RESERVED_WORD"); // "зі"
        consume("GEOMETRY"); // "стороною"

        String lineName = consumeAndGet("NAME").getLexeme(); // <лінія>

        String pointAName = lineName.charAt(0) + "";
        String pointBName = lineName.charAt(1) + "";

        String coordinatesA = getCoords(pointAName);
        String coordinatesB = getCoords(pointBName);

        String[] coordsA = coordinatesA.replaceAll("[()]", "").split(",");
        String[] coordsB = coordinatesB.replaceAll("[()]", "").split(",");

        int x1 = Integer.parseInt(coordsA[0].trim());
        int y1 = Integer.parseInt(coordsA[1].trim());
        int x2 = Integer.parseInt(coordsB[0].trim());
        int y2 = Integer.parseInt(coordsB[1].trim());

        int dx = x2 - x1;
        int dy = y2 - y1;
        int x3 = x1 - dy;
        int y3 = y1 + dx;
        int x4 = x2 - dy;
        int y4 = y2 + dx;

        String pointCName = pointAName + "1";
        String pointDName = pointBName + "1";

        // Create a node for the square and add points and edges
        Node buildSquareNode = new Node("BuildSquare");
        buildSquareNode.addChild(new Node("PutPoint: " + pointAName + " (" + x1 + "," + y1 + ")"));
        buildSquareNode.addChild(new Node("PutPoint: " + pointBName + " (" + x2 + "," + y2 + ")"));
        buildSquareNode.addChild(new Node("PutPoint: " + pointCName + " (" + x3 + "," + y3 + ")"));
        buildSquareNode.addChild(new Node("PutPoint: " + pointDName + " (" + x4 + "," + y4 + ")"));

        // Add lines connecting the square's corners with named endpoints
        buildSquareNode.addChild(new Node("DrawSegment: " + pointAName + " (" + x1 + "," + y1 + ") to " + pointBName + " (" + x2 + "," + y2 + ")"));
        buildSquareNode.addChild(new Node("DrawSegment: " + pointCName + " (" + x3 + "," + y3 + ") to " + pointDName + " (" + x4 + "," + y4 + ")"));
        buildSquareNode.addChild(new Node("DrawSegment: " + pointAName + " (" + x1 + "," + y1 + ") to " + pointCName + " (" + x3 + "," + y3 + ")"));
        buildSquareNode.addChild(new Node("DrawSegment: " + pointBName + " (" + x2 + "," + y2 + ") to " + pointDName + " (" + x4 + "," + y4 + ")"));


        // Add point geometries for the square
        geometries.add(new SegmentGeometry(x1, y1, x3, y3));
        geometries.add(new SegmentGeometry(x1, y1, x2, y2));
        geometries.add(new SegmentGeometry(x2, y2, x4, y4));
        geometries.add(new SegmentGeometry(x3, y3, x4, y4));
        geometries.add(new PointGeometry(pointCName, x1, y1));
        geometries.add(new PointGeometry(pointCName, x2, y2));
        geometries.add(new PointGeometry(pointAName, x3, y3));
        geometries.add(new PointGeometry(pointBName, x4, y4));

        graphicCode += "       geometries.add(new SegmentGeometry("+x1+","+ y1+","+ x3+","+y3+"));\n";
        graphicCode += "       geometries.add(new SegmentGeometry("+x1+","+ y1+","+ x2+","+y2+"));\n";
        graphicCode += "       geometries.add(new SegmentGeometry("+x2+","+ y2+","+ x4+","+y4+"));\n";
        graphicCode += "       geometries.add(new SegmentGeometry("+x3+","+ y3+","+ x4+","+y4+"));\n";
        graphicCode += "       geometries.add(new PointGeometry(\""+pointCName+"\","+ x3+","+ y3+"));\n";
        graphicCode += "       geometries.add(new PointGeometry(\""+pointDName+"\","+ x4+","+ y4+"));\n";
        graphicCode += "       geometries.add(new PointGeometry(\""+pointAName+"\","+ x2+","+ y2+"));\n";
        graphicCode += "       geometries.add(new PointGeometry(\""+pointBName+"\","+ x1+","+ y1+"));\n";



        return buildSquareNode;
    }


    private Node parseListOfPoints() {
        Node listNode = new Node("ListOfPoints");

        String pointName = consumeAndGet("NAME").getLexeme(); // <точка>
        String coordinates = consumeAndGet("COORDINATES").getLexeme(); // <координати>

        pointCoordinates.put(pointName, coordinates);
        listNode.addChild(new Node(pointName + " " + coordinates));

        while (currentTokenIndex < tokens.size() && currentToken().getLexeme().equals(",")) {
            consume("DELIMITER"); // consume ','
            pointName = consumeAndGet("NAME").getLexeme(); // <точка>
            coordinates = consumeAndGet("COORDINATES").getLexeme(); // <координати>

            pointCoordinates.put(pointName, coordinates); // Store point coordinates
            listNode.addChild(new Node(pointName + " " + coordinates));
        }

        return listNode;
    }


    private Node handleDrawPerpendicular() {
        // Start parsing the command
        consume("GEOMETRY"); // "пряму"
        consume("DELIMITER"); // ","
        consume("GEOMETRY"); // "перпендикулярну"
        consume("RESERVED_WORD"); // "до"
        consume("GEOMETRY"); // "відрізка"

        String lineName = consumeAndGet("NAME").getLexeme(); // <лінія>

        String pointA = lineName.charAt(0) + "";
        String pointB = lineName.charAt(1) + "";

        String coordinatesA = pointCoordinates.getOrDefault(pointA, "(0,0)");
        String coordinatesB = pointCoordinates.getOrDefault(pointB, "(0,0)");

        String[] coordsA = coordinatesA.replaceAll("[()]", "").split(",");
        String[] coordsB = coordinatesB.replaceAll("[()]", "").split(",");
        int xA = Integer.parseInt(coordsA[0].trim());
        int yA = Integer.parseInt(coordsA[1].trim());
        int xB = Integer.parseInt(coordsB[0].trim());
        int yB = Integer.parseInt(coordsB[1].trim());

        int midX = (xA + xB) / 2;
        int midY = (yA + yB) / 2;

        double slopeAB = (yB - yA) / (double)(xB - xA);
        double perpendicularSlope = -1 / slopeAB;

        int length = 20;

        int dx = (int)(length / Math.sqrt(1 + Math.pow(perpendicularSlope, 2)));
        int dy = (int)(perpendicularSlope * dx);

        int xC1 = midX + dx;
        int yC1 = midY + dy;
        int xC2 = midX - dx;
        int yC2 = midY - dy;

        Node drawPerpendicularNode = new Node("DrawPerpendicular");
        drawPerpendicularNode.addChild(new Node(pointA + " " + coordinatesA));
        drawPerpendicularNode.addChild(new Node(pointB + " " + coordinatesB));
        drawPerpendicularNode.addChild(new Node("C1 (" + xC1 + "," + yC1 + ")"));
        drawPerpendicularNode.addChild(new Node("C2 (" + xC2 + "," + yC2 + ")"));

        geometries.add(new SegmentGeometry(midX, midY, xC1, yC1));
        geometries.add(new SegmentGeometry(midX, midY, xC2, yC2));


        graphicCode += "       geometries.add(new SegmentGeometry("+midX+","+ midY+","+ xC1+","+yC1+"));\n";
        graphicCode += "       geometries.add(new SegmentGeometry("+midX+","+ midY+","+ xC2+","+yC2+"));\n";



        return drawPerpendicularNode;
    }


}
