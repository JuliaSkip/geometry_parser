import java.util.List;
import java.util.Set;
import java.util.HashSet;

class SemanticAnalyzer {
    private Set<String> pointNames;
    private Set<String> lineNames;

    public SemanticAnalyzer() {
        this.pointNames = new HashSet<>();
        this.lineNames = new HashSet<>();
    }

    public void analyze(Node node) {
        traverseNode(node);
    }

    private void traverseNode(Node node) {
        switch (node.getType()) {
            case "PutPoint":
                handlePutPoint(node);
                break;
            case "ConnectPoints":
                handleConnectPoints(node);
                break;
            case "DrawSegment":
                handleDrawSegment(node);
                break;
            case "BuildTriangle":
                handleBuildTriangle(node);
                break;
            case "BuildSquare":
                handleBuildSquare(node);
                break;
            case "DrawPerpendicular":
                handleDrawPerpendicular(node);
                break;
            default:
                for (Node child : node.getChildren()) {
                    traverseNode(child);
                }
                break;
        }
    }

    private void handlePutPoint(Node node) {
        String pointName = node.getChildren().get(0).getType();
        if (pointNames.contains(pointName)) {
            System.out.println("Semantic Error: Point " + pointName + " is already defined.");
        }
        pointNames.add(pointName);
    }

    private void handleConnectPoints(Node node) {
        for (Node child : node.getChildren()) {
            if (child.getType().equals("DrawSegment")) continue;
            String pointName = child.getChildren().get(0).getType();
            String pointCoords = child.getChildren().get(1).getType();
            System.out.println(pointCoords+""+pointName);
            if (!pointNames.contains(pointName) && pointCoords == null) {
                System.out.println("Semantic Error: Point " + pointName + " is not defined.");
            }
        }
    }

    private void handleDrawSegment(Node node) {
        String pointA = node.getChildren().get(0).getType().split(" ")[0];
        String pointAC = node.getChildren().get(0).getType().split(" ")[1];
        String pointB = node.getChildren().get(1).getType().split(" ")[0];
        String pointBC = node.getChildren().get(1).getType().split(" ")[1];

        if ((!pointNames.contains(pointA) || !pointNames.contains(pointB)) && (pointBC == null && pointAC == null)) {
            System.out.println("Semantic Error: Points " + pointA + " or " + pointB + " are not defined for segment.");
        }

        lineNames.add(pointA + pointB);
    }

    private void handleDrawPerpendicular(Node node) {
        String pointA = node.getChildren().get(0).getType().split(" ")[0];
        String pointAC = node.getChildren().get(0).getType().split(" ")[1];
        String pointB = node.getChildren().get(1).getType().split(" ")[0];
        String pointBC = node.getChildren().get(1).getType().split(" ")[1];

        if ((!pointNames.contains(pointA) || !pointNames.contains(pointB)) && (pointBC == null && pointAC == null)) {
            System.out.println("Semantic Error: Points " + pointA + " or " + pointB + " are not defined for perpendicular line.");
        }

        String lineName = pointA + pointB;
        if (!lineNames.contains(lineName)) {
            System.out.println("Semantic Error: Line " + lineName + " is not defined for perpendicular line.");
        }
    }

    private void handleBuildTriangle(Node node) {
        List<Node> points = node.getChildren();
        if (points.size() != 3) {
            System.out.println("Semantic Error: Triangle requires 3 points.");
        }

        for (Node point : points) {
            String pointName = point.getType().split(" ")[0];
            String pointCoords = point.getType().split(" ")[1];
            if (!pointNames.contains(pointName) && pointCoords == null) {
                System.out.println("Semantic Error: Point " + pointName + " is not defined for triangle.");
            }
        }
    }

    private void handleBuildSquare(Node node) {
        List<Node> children = node.getChildren();
        int putPointCount = 0;
        int drawSegmentCount = 0;
        Set<String> squarePoints = new HashSet<>();

        for (Node child : children) {
            String action = child.getType();

            if (action.startsWith("PutPoint")) {
                String pointName = child.getType().split(": ")[1].split(" ")[0];
                String pointCoords = child.getType().split(": ")[1].split(" ")[1];

                if (!pointNames.contains(pointName) && pointCoords == null) {
                    System.out.println("Semantic Error: Point " + pointName + " is not defined.");
                }

                squarePoints.add(pointName);
                putPointCount++;
            }

            else if (action.startsWith("DrawSegment")) {
                String[] points = child.getType().split(": ")[1].split(" to ");
                String pointA = points[0].split(" ")[0];
                String pointB = points[1].split(" ")[0];
                String pointAC = points[0].split(" ")[1];
                String pointBC = points[1].split(" ")[1];

                if ((!pointNames.contains(pointA) || !pointNames.contains(pointB)) && (pointBC == null && pointAC == null)) {
                    System.out.println("Semantic Error: Points " + pointA + " or " + pointB + " are not defined.");
                }

                drawSegmentCount++;
            }
        }
        if (putPointCount != 4) {
            System.out.println("Semantic Error: Square requires exactly 4 PutPoint actions, found " + putPointCount);
        }
        if (drawSegmentCount != 4) {
            System.out.println("Semantic Error: Square requires exactly 4 DrawSegment actions, found " + drawSegmentCount);
        }
        if (squarePoints.size() != 4) {
            System.out.println("Semantic Error: Square requires 4 unique points.");
        }
    }


}
