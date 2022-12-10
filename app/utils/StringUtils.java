package utils;

import java.util.List;

public class StringUtils {
    public static String join(List<? extends Object> objectList, String separator) {
        if (objectList == null || objectList.isEmpty()) {
            return "";
        }
        String string = objectList.get(0).toString();
        for (int i = 1; i < objectList.size(); i++) {
            string += separator;
            string += objectList.get(i);
        }
        return string;
    }
}
