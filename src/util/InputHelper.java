package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.Set;

/** Shared console input helpers with validation loops so bad input never crashes a menu. */
public final class InputHelper {

    private InputHelper() {}

    public static String readText(Scanner sc, String label, int minLen, int maxLen, boolean allowBlank) {
        while (true) {
            System.out.print(label);
            String v = sc.nextLine().trim();
            if (v.isBlank()) {
                if (allowBlank) return "";
                System.out.println("  ! Input cannot be blank. Please try again.");
                continue;
            }
            if (v.length() < minLen) {
                System.out.println("  ! Too short (min " + minLen + " chars). Please try again.");
                continue;
            }
            if (v.length() > maxLen) {
                System.out.println("  ! Too long (max " + maxLen + " chars). Please try again.");
                continue;
            }
            return v;
        }
    }

    public static String readText(Scanner sc, String label) {
        return readText(sc, label, 1, 100, false);
    }

    /** Blank allowed -> returns "" when skipped (useful for optional update fields). */
    public static String readOptionalText(Scanner sc, String label, int maxLen) {
        return readText(sc, label, 1, maxLen, true);
    }

    public static int readInt(Scanner sc, String label, int min, int max) {
        while (true) {
            System.out.print(label);
            String raw = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(raw);
                if (v < min || v > max) {
                    System.out.println("  ! Enter a whole number between " + min + " and " + max + ".");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("  ! Invalid number. Enter digits only (e.g. 5).");
            }
        }
    }

    public static int readId(Scanner sc, String label) {
        return readInt(sc, label, 1, Integer.MAX_VALUE);
    }

    public static double readDouble(Scanner sc, String label, double min, double max) {
        while (true) {
            System.out.print(label);
            String raw = sc.nextLine().trim().replace(",", "");
            try {
                double v = Double.parseDouble(raw);
                if (v < min || v > max) {
                    System.out.println("  ! Enter an amount between " + min + " and " + max + ".");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("  ! Invalid amount. Example: 150.00");
            }
        }
    }

    public static String readEmail(Scanner sc, String label, boolean allowBlank) {
        while (true) {
            String v = readText(sc, label, 1, 100, allowBlank);
            if (allowBlank && v.isBlank()) return "";
            if (!v.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                System.out.println("  ! Invalid email format. Example: juan@email.com");
                continue;
            }
            return v;
        }
    }

    public static String readContact(Scanner sc, String label, boolean allowBlank) {
        while (true) {
            String v = readText(sc, label, 1, 20, allowBlank);
            if (allowBlank && v.isBlank()) return "";
            String digits = v.replaceAll("[^0-9]", "");
            if (digits.length() < 7 || digits.length() > 13) {
                System.out.println("  ! Contact must contain 7-13 digits. Example: 0917-123-4567");
                continue;
            }
            return v;
        }
    }

    public static LocalDate readDate(Scanner sc, String label, boolean rejectPast) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (true) {
            System.out.print(label);
            String raw = sc.nextLine().trim();
            try {
                LocalDate d = LocalDate.parse(raw, fmt);
                if (rejectPast && d.isBefore(LocalDate.now())) {
                    System.out.println("  ! Date cannot be in the past. Use today or later (yyyy-MM-dd).");
                    continue;
                }
                return d;
            } catch (DateTimeParseException e) {
                System.out.println("  ! Invalid date. Use format yyyy-MM-dd (e.g. 2026-09-20).");
            }
        }
    }

    public static <E extends Enum<E>> E readEnum(Scanner sc, String label, Class<E> type) {
        E[] values = type.getEnumConstants();
        while (true) {
            System.out.print(label);
            String raw = sc.nextLine().trim().toUpperCase().replace(" ", "_").replace("-", "_");
            for (E v : values) {
                if (v.name().equals(raw)) return v;
            }
            System.out.print("  ! Invalid value. Choose one of: ");
            for (int i = 0; i < values.length; i++) {
                System.out.print(values[i].name() + (i < values.length - 1 ? ", " : "\n"));
            }
        }
    }

    public static String readOption(Scanner sc, String label, Set<String> allowed) {
        while (true) {
            System.out.print(label);
            String v = sc.nextLine().trim().toUpperCase();
            if (allowed.contains(v)) return v;
            System.out.println("  ! Invalid option. Allowed: " + String.join("/", allowed));
        }
    }

    public static boolean confirm(Scanner sc, String label) {
        while (true) {
            System.out.print(label + " (Y/N): ");
            String v = sc.nextLine().trim().toUpperCase();
            if (v.equals("Y") || v.equals("YES")) return true;
            if (v.equals("N") || v.equals("NO")) return false;
            System.out.println("  ! Type Y or N.");
        }
    }

    public static void pause(Scanner sc) {
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
}
