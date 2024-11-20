import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



class Lexeme {
    private String lexeme;
    private String type;

    public Lexeme(String lexeme, String type) {
        this.lexeme = lexeme;
        this.type = type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public String getType() {
        return type;
    }
}

public class LexicalAnalyser {
    private static final LinkedHashMap<String, Pattern> patterns = new LinkedHashMap<>();

    static {
        patterns.put("CONDITION_SIGN", Pattern.compile("(<=|>=|==|<|>|!=|&&|\\|\\|)"));
        patterns.put("ARITHMETIC_OPERATOR", Pattern.compile("[+\\-*/=%]"));
        patterns.put("DELIMITER", Pattern.compile("[\\[\\](){}.,!?;:'\"\\\\]"));
        patterns.put("BUILTIN_FUNCTION", Pattern.compile("(sin|cos|tan|log|exp|max|min)"));
        patterns.put("FLOAT", Pattern.compile("\\d+\\.\\d+"));
        patterns.put("INTEGER", Pattern.compile("\\d+"));





        patterns.put("COORDINATES", Pattern.compile("\\s*\\(\\s*-?[0-9]+(\\.[0-9]+)?\\s*,\\s*-?[0-9]+(\\.[0-9]+)?\\s*\\)"));


        patterns.put("RESERVED_WORD", Pattern.compile(
                "(if|else|while|char|double|float|String|int|void|false|true|null|private|public|static|return|" +
                        "ЗАДАНО|ПОСТАВИТИ|З'ЄДНАТИ|ПРОВЕСТИ|ПОБУДУВАТИ|ЧЕРЕЗ|ЗА|ТА|ДО|задано|поставити|зʼєднати|провести|побудувати|через|за|та|зі|дві|до)"
        ));

        patterns.put("GEOMETRY", Pattern.compile(
                "(ТОЧК[А-Я]*|ТРИКУТН[А-Я]*|ВІДРІЗ[А-Я]*|точк[а-я]*|трикутн[а-я]*|відріз[а-я]*|перпендикул[а-я]*|сторон[а-я]*|квадрат[a-z]*|прям[а-я]*|бісектр[а-я]*|кут[а-я]*)"
        ));

        patterns.put("NAME",Pattern.compile("[А-Яа-яA-Za-z][А-Яа-яA-Za-z0-9]*"));

        patterns.put("IDENTIFIER",Pattern.compile("[А-Яа-яA-Za-z][А-Яа-яA-Za-z0-9]*"));

    }

    public static ArrayList<Lexeme> analyse(String text) {
        ArrayList<Lexeme> result = new ArrayList<>();
        String[] words = text.split("\\s+");

        for (String word : words) {
            boolean matched = false;

            for (HashMap.Entry<String, Pattern> entry : patterns.entrySet()) {
                String name = entry.getKey();
                Pattern pattern = entry.getValue();

                Matcher matcher = pattern.matcher(word);
                if (matcher.matches()) {
                    result.add(new Lexeme(word, name));
                    matched = true;
                    break;
                }
            }

            if (!matched) result.add(new Lexeme(word, "ERROR"));
        }

        return result;
    }

}