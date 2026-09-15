package util;

import java.util.ArrayList;
import java.util.List;

/** Prints formal ASCII tables: +------+ borders, padded columns, centered title. */
public final class TablePrinter {

    private TablePrinter() {}

    public static void print(String title, String[] headers, List<String[]> rows) {
        List<String[]> safe = rows == null ? new ArrayList<>() : rows;
        int cols = headers.length;
        int[] width = new int[cols];
        for (int i = 0; i < cols; i++) {
            width[i] = headers[i] == null ? 0 : headers[i].length();
        }
        for (String[] r : safe) {
            for (int i = 0; i < cols; i++) {
                String cell = (r != null && i < r.length && r[i] != null) ? r[i] : "";
                if (cell.length() > width[i]) width[i] = cell.length();
            }
        }
        // Keep tables readable: cap very long cells.
        for (int i = 0; i < cols; i++) {
            if (width[i] > 30) width[i] = 30;
        }

        String border = buildBorder(width);
        int tableWidth = border.length();

        if (title != null && !title.isBlank()) {
            System.out.println(center(title.toUpperCase(), tableWidth));
        }
        System.out.println(border);
        System.out.println(buildRow(headers, width));
        System.out.println(border);
        if (safe.isEmpty()) {
            System.out.println(center("( NO RECORDS FOUND )", tableWidth));
        } else {
            for (String[] r : safe) {
                String[] cells = new String[cols];
                for (int i = 0; i < cols; i++) {
                    String cell = (r != null && i < r.length && r[i] != null) ? r[i] : "-";
                    if (cell.length() > width[i]) cell = cell.substring(0, width[i] - 1) + ".";
                    cells[i] = cell;
                }
                System.out.println(buildRow(cells, width));
            }
        }
        System.out.println(border);
        System.out.println("Total record(s): " + safe.size());
    }

    private static String buildBorder(int[] width) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : width) {
            sb.append("-".repeat(w + 2)).append("+");
        }
        return sb.toString();
    }

    private static String buildRow(String[] cells, int[] width) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < width.length; i++) {
            String c = (cells != null && i < cells.length && cells[i] != null) ? cells[i] : "";
            sb.append(" ").append(padRight(c, width[i])).append(" |");
        }
        return sb.toString();
    }

    private static String padRight(String s, int n) {
        if (s.length() >= n) return s;
        return s + " ".repeat(n - s.length());
    }

    private static String center(String s, int width) {
        if (s.length() >= width) return s;
        int left = (width - s.length()) / 2;
        return " ".repeat(left) + s;
    }
}
