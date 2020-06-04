/*
 * MIT License
 *
 * Copyright (C) 2020 SimpleCloud-Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.fusesource.jansi;

import org.fusesource.jansi.Ansi.Attribute;
import org.fusesource.jansi.Ansi.Color;

import java.io.IOException;
import java.util.Locale;

/**
 * Renders ANSI color escape-codes in strings by parsing out some special syntax to pick up the correct fluff to use.
 *
 * The syntax for embedded ANSI codes is:
 *
 * <pre>
 *   <tt>@|</tt><em>code</em>(<tt>,</tt><em>code</em>)* <em>text</em><tt>|@</tt>
 * </pre>
 *
 * Examples:
 *
 * <pre>
 *   <tt>@|bold Hello|@</tt>
 * </pre>
 *
 * <pre>
 *   <tt>@|bold,red Warning!|@</tt>
 * </pre>
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 * @since 1.1
 */
public class AnsiRenderer {

    public static final String BEGIN_TOKEN = "@|";

    public static final String END_TOKEN = "|@";

    public static final String CODE_TEXT_SEPARATOR = " ";

    public static final String CODE_LIST_SEPARATOR = ",";

    private static final int BEGIN_TOKEN_LEN = 2;

    private static final int END_TOKEN_LEN = 2;

    public static String render(final String input) throws IllegalArgumentException {
        try {
            return render(input, new StringBuilder(input.length())).toString();
        } catch (IOException e) {
            // Cannot happen because StringBuilder does not throw IOException
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Renders the given input to the target Appendable.
     *
     * @param input
     *            source to render
     * @param target
     *            render onto this target Appendable.
     * @return the given Appendable
     * @throws IOException
     *             If an I/O error occurs
     */
    public static Appendable render(final String input, Appendable target) throws IOException {

        int i = 0;
        int j, k;

        while (true) {
            j = input.indexOf(BEGIN_TOKEN, i);
            if (j == -1) {
                if (i == 0) {
                    target.append(input);
                    return target;
                }
                target.append(input.substring(i, input.length()));
                return target;
            }
            target.append(input.substring(i, j));
            k = input.indexOf(END_TOKEN, j);

            if (k == -1) {
                target.append(input);
                return target;
            }
            j += BEGIN_TOKEN_LEN;
            String spec = input.substring(j, k);

            String[] items = spec.split(CODE_TEXT_SEPARATOR, 2);
            if (items.length == 1) {
                target.append(input);
                return target;
            }
            String replacement = render(items[1], items[0].split(CODE_LIST_SEPARATOR));

            target.append(replacement);

            i = k + END_TOKEN_LEN;
        }
    }

    public static String render(final String text, final String... codes) {
        return render(Ansi.ansi(), codes)
                .a(text).reset().toString();
    }

    /**
     * Renders {@link Code} names as an ANSI escape string.
     * @param codes The code names to render
     * @return an ANSI escape string.
     */
    public static String renderCodes(final String... codes) {
        return render(Ansi.ansi(), codes).toString();
    }

    /**
     * Renders {@link Code} names as an ANSI escape string.
     * @param codes A space separated list of code names to render
     * @return an ANSI escape string.
     */
    public static String renderCodes(final String codes) {
        return renderCodes(codes.split("\\s"));
    }

    private static Ansi render(Ansi ansi, String... names) {
        for (String name : names) {
            render(ansi, name);
        }
        return ansi;
    }

    private static Ansi render(Ansi ansi, String name) {
        Code code = Code.valueOf(name.toUpperCase(Locale.ENGLISH));
        if (code.isColor()) {
            if (code.isBackground()) {
                ansi.bg(code.getColor());
            } else {
                ansi.fg(code.getColor());
            }
        } else if (code.isAttribute()) {
            ansi.a(code.getAttribute());
        }
        return ansi;
    }

    public static boolean test(final String text) {
        return text != null && text.contains(BEGIN_TOKEN);
    }

    public enum Code {
        //
        // TODO: Find a better way to keep Code in sync with Color/Attribute/Erase
        //

        // Colors
        BLACK(Color.BLACK),
        RED(Color.RED),
        GREEN(Color.GREEN),
        YELLOW(Color.YELLOW),
        BLUE(Color.BLUE),
        MAGENTA(Color.MAGENTA),
        CYAN(Color.CYAN),
        WHITE(Color.WHITE),

        // Foreground Colors
        FG_BLACK(Color.BLACK, false),
        FG_RED(Color.RED, false),
        FG_GREEN(Color.GREEN, false),
        FG_YELLOW(Color.YELLOW, false),
        FG_BLUE(Color.BLUE, false),
        FG_MAGENTA(Color.MAGENTA, false),
        FG_CYAN(Color.CYAN, false),
        FG_WHITE(Color.WHITE, false),

        // Background Colors
        BG_BLACK(Color.BLACK, true),
        BG_RED(Color.RED, true),
        BG_GREEN(Color.GREEN, true),
        BG_YELLOW(Color.YELLOW, true),
        BG_BLUE(Color.BLUE, true),
        BG_MAGENTA(Color.MAGENTA, true),
        BG_CYAN(Color.CYAN, true),
        BG_WHITE(Color.WHITE, true),

        // Attributes
        RESET(Attribute.RESET),
        INTENSITY_BOLD(Attribute.INTENSITY_BOLD),
        INTENSITY_FAINT(Attribute.INTENSITY_FAINT),
        ITALIC(Attribute.ITALIC),
        UNDERLINE(Attribute.UNDERLINE),
        BLINK_SLOW(Attribute.BLINK_SLOW),
        BLINK_FAST(Attribute.BLINK_FAST),
        BLINK_OFF(Attribute.BLINK_OFF),
        NEGATIVE_ON(Attribute.NEGATIVE_ON),
        NEGATIVE_OFF(Attribute.NEGATIVE_OFF),
        CONCEAL_ON(Attribute.CONCEAL_ON),
        CONCEAL_OFF(Attribute.CONCEAL_OFF),
        UNDERLINE_DOUBLE(Attribute.UNDERLINE_DOUBLE),
        UNDERLINE_OFF(Attribute.UNDERLINE_OFF),

        // Aliases
        BOLD(Attribute.INTENSITY_BOLD),
        FAINT(Attribute.INTENSITY_FAINT),;

        private final Enum<?> n;

        private final boolean background;

        Code(final Enum<?> n, boolean background) {
            this.n = n;
            this.background = background;
        }

        Code(final Enum<?> n) {
            this(n, false);
        }

        public boolean isColor() {
            return n instanceof Ansi.Color;
        }

        public Ansi.Color getColor() {
            return (Ansi.Color) n;
        }

        public boolean isAttribute() {
            return n instanceof Attribute;
        }

        public Attribute getAttribute() {
            return (Attribute) n;
        }

        public boolean isBackground() {
            return background;
        }
    }

    private AnsiRenderer() {
    }

}