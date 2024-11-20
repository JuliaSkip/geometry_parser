import javax.swing.*;
import java.util.ArrayList;
/**
 * <текст> ::= <операція> <наступні операції>
 * <наступні операції>::= ; <текст> | <пусто>
 * <операція> ::= <поставити> | <провести> | <зʼєднати> | <побудувати>
 *
 * <поставити> ::= "поставити точку"  <точка>
 * <точка> ::= <ідентифікатор> <координати>?
 *
 * <провести> ::= "провести" <лінія>
 * <лінія> ::= <відрізок> | <перпендикуляр>
 * <відрізок> ::= "відрізок через дві точки" <точка>  "та"  <точка>
 * <перпендикуляр> ::= "пряму , перпендикулярну до відрізка" <назва>
 *
 * <зʼєднати> ::= "зʼєднати точки" <список точок>
 *
 * <побудувати> ::= "побудувати" <фігура>
 * <фігура> ::= <трикутник> | <квадрат>
 * <трикутник> ::= "трикутник за точками"  <точка> "," <точка> "," <точка>
 * <квадрат> ::= "квадрат зі стороною" <назва>
 *
 * <координати> ::= "(" <число> "," <число> ")"
 * <список точок> ::= <точка> | <точка> "," <список точок>
 */

public class Main {
    public static void main(String[] args) {
        LexicalAnalyser analyser = new LexicalAnalyser();
        int sentenceNumber = 12;

        String[] testSentences = {
                "поставити точку A ",
                "поставити точку A ; поставити точку B  ; поставити точку C (0,5) ; поставити точку D (5,0)",
                "поставити точку A (3,1) ; провести відрізок через дві точки C (1,2) та B",
                "провести відрізок через дві точки C (-5,5) та B (5,-5) ; провести відрізок через дві точки A та M",
                "провести відрізок через дві точки C (-5,5) та B (5,-5) ; провести пряму , перпендикулярну до відрізка CB",
                "поставити точку M (1,2) ; поставити точку B (1,6) ; провести відрізок через дві точки M та B",
                "поставити точку A (1,2) ; поставити точку B (1,6) ; провести відрізок через дві точки A та B ; провести пряму , перпендикулярну до відрізка AB",
                "зʼєднати точки A (1,1) , B (-5,6) , C (9,2) , D (-2,2) ", //додати відсутність координат
                "побудувати трикутник за точками A (-4,0) , B (4,0) , C (0,5)",
                "побудувати трикутник за точками A , B , C ",
                "поставити точку A ; поставити точку B ; провести відрізок через дві точки A та B ; побудувати квадрат зі стороною AB",
                "провести відрізок через дві точки A та B ; побудувати квадрат зі стороною AB",
                "побудувати квадрат зі стороною AB",


        };

        ArrayList<Lexeme> lexemes = analyser.analyse(testSentences[sentenceNumber]);

        for (Lexeme lexeme : lexemes) {
            System.out.print( "["+lexeme.getType() + ": "+ lexeme.getLexeme()+"]");
        }

        Parser parser = new Parser(lexemes);
        Node programNode = parser.parse();



        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        semanticAnalyzer.analyze(programNode);

        System.out.println("\n"+programNode.toString());

        parser.saveCodeToFile();

        JFrame frame = new JFrame("Geometry Drawer Main");
        ArrayList<Geometry> geometries = parser.geometries;
        DrawingPanel drawingPanel = new DrawingPanel(geometries);
        frame.add(drawingPanel);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);

    }

}
