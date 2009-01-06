package net.nodebox.graphics;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;
import java.util.Iterator;

public class Text extends Grob {

    public enum Align {
        LEFT, RIGHT, CENTER, JUSTIFY
    }

    private String text;
    private double baseLineX, baseLineY;
    private double width = Double.MAX_VALUE;
    private double height = Double.MAX_VALUE;
    private String fontName = "Helvetica";
    private double fontSize = 24;
    private double lineHeight;
    private Align align = Align.LEFT;
    private Color fillColor = new Color();

    public Text(String text, Point pt) {
        this.text = text;
        this.baseLineX = pt.getX();
        this.baseLineY = pt.getY();
    }

    public Text(String text, double baseLineX, double baseLineY) {
        this.text = text;
        this.baseLineX = baseLineX;
        this.baseLineY = baseLineY;
    }

    public Text(String text, Rect r) {
        this.text = text;
        this.baseLineX = r.getX();
        this.baseLineY = r.getY();
        this.width = r.getWidth();
        this.height = r.getHeight();
    }

    public Text(String text, double x, double y, double width, double height) {
        this.text = text;
        this.baseLineX = x;
        this.baseLineY = y;
        this.width = width;
        this.height = height;
    }

    public Text(Text other) {
        super(other);
        this.text = other.text;
        this.baseLineX = other.baseLineX;
        this.baseLineY = other.baseLineY;
        this.width = other.width;
        this.height = other.height;
        this.fontName = other.fontName;
        this.fontSize = other.fontSize;
        this.lineHeight = other.lineHeight;
        this.align = other.align;
        fillColor = other.fillColor == null ? null : other.fillColor.clone();
    }

    //// Getters/setters /////

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getBaseLineX() {
        return baseLineX;
    }

    public void setBaseLineX(double baseLineX) {
        this.baseLineX = baseLineX;
    }

    public double getBaseLineY() {
        return baseLineY;
    }

    public void setBaseLineY(double baseLineY) {
        this.baseLineY = baseLineY;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public double getFontSize() {
        return fontSize;
    }

    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
    }

    public Font getFont() {
        return new Font(fontName, Font.PLAIN, (int) fontSize);
    }

    public double getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(double lineHeight) {
        this.lineHeight = lineHeight;
    }

    public Align getAlign() {
        return align;
    }

    public void setAlign(Align align) {
        this.align = align;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    //// Font management ////

    public static boolean fontExists(String fontName) {
        // TODO: Move getAllFonts() in static attribute.
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] allFonts = env.getAllFonts();
        for (Font font : allFonts) {
            if (font.getName().equals(fontName)) {
                return true;
            }
        }
        return false;
    }

    //// Metrics ////

    private TextLayout getTextLayout() {
        // TODO: Incorporate LineBreakMeasurer

        AffineTransform trans = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(trans, true, true);
        AttributedString attrString = new AttributedString(text);
        attrString.addAttribute(TextAttribute.FONT, getFont());
        attrString.addAttribute(TextAttribute.FOREGROUND, fillColor.getAwtColor());
        if (align == Align.RIGHT) {
            attrString.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_RTL);
        } else if (align == Align.CENTER) {
            // TODO: Center alignment?
        } else if (align == Align.JUSTIFY) {
            attrString.addAttribute(TextAttribute.JUSTIFICATION, TextAttribute.JUSTIFICATION_FULL);
        }
        TextLayout layout = new TextLayout(attrString.getIterator(), frc);
        return layout;
    }

    private AttributedString getStyledText() {
        AttributedString attrString = new AttributedString(text);
        attrString.addAttribute(TextAttribute.FONT, getFont());
        attrString.addAttribute(TextAttribute.FOREGROUND, fillColor.getAwtColor());
        if (align == Align.RIGHT) {
            attrString.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_RTL);
        } else if (align == Align.CENTER) {
            // TODO: Center alignment?
        } else if (align == Align.JUSTIFY) {
            attrString.addAttribute(TextAttribute.JUSTIFICATION, TextAttribute.JUSTIFICATION_FULL);
        }
        return attrString;
    }

    public Rect getMetrics() {
        if (text == null || text.length() == 0) return new Rect();
        TextLayoutIterator iterator = new TextLayoutIterator();
        Rectangle2D bounds = new Rectangle2D.Double();
        while (iterator.hasNext()) {
            TextLayout layout = iterator.next();
            // TODO: Compensate X, Y
            bounds = bounds.createUnion(layout.getBounds());
        }
        return new Rect(bounds);
    }

    //// Drawing ////

    public void draw(Graphics2D g) {
        if (text == null || text.length() == 0) return;
        // g.setColor(fillColor.getAwtColor());
        //getTextLayout().draw(g, (float) baseLineX, (float) baseLineY);

        TextLayoutIterator iterator = new TextLayoutIterator();
        while (iterator.hasNext()) {
            TextLayout layout = iterator.next();
            layout.draw(g, iterator.getX(), iterator.getY());
        }
        /*
        double x = baseLineX;
        double y = baseLineY;
        FontRenderContext frc = g.getFontRenderContext();
        AttributedString styledText = getStyledText();
        LineBreakMeasurer measurer = new LineBreakMeasurer(styledText.getIterator(), frc);
        while (measurer.getPosition() < text.length()) {
            TextLayout layout = measurer.nextLayout((float) width);
            double dx = 0;
            if (align == Align.RIGHT) {
                dx = width - layout.getAdvance();
            } else if (align == Align.CENTER) {
                dx = (width - layout.getAdvance()) / 2.0F;
            } else if (align == Align.JUSTIFY) {
                // Don't justify the last line.
                if (measurer.getPosition() < text.length()) {
                    layout = layout.getJustifiedLayout((float) width);
                }
            }
            layout.draw(g, (float) (x + dx), (float) y);
        }
        */
    }


    public BezierPath getPath() {
        BezierPath p = new BezierPath();
        TextLayoutIterator iterator = new TextLayoutIterator();
        while (iterator.hasNext()) {
            TextLayout layout = iterator.next();
            AffineTransform trans = new AffineTransform();
            trans.translate(iterator.getX(), iterator.getY());
            Shape shape = layout.getOutline(trans);
            p.extend(shape);
        }
        return p;
    }

    public Rect getBounds() {
        // TODO: This is correct, but creating a full path just for measuring bounds is slow.
        return getPath().getBounds();
    }

    public Text clone() {
        return new Text(this);
    }

    private class TextLayoutIterator implements Iterator<TextLayout> {

        private float x, y;
        private float ascent;
        private AttributedString styledText;
        private LineBreakMeasurer measurer;
        private boolean first;

        private TextLayoutIterator() {
            x = (float) baseLineX;
            y = (float) baseLineY;
            styledText = getStyledText();
            measurer = new LineBreakMeasurer(styledText.getIterator(), new FontRenderContext(new AffineTransform(), true, true));
            first = true;
        }

        public boolean hasNext() {
            return measurer.getPosition() < text.length();
        }

        public TextLayout next() {
            if (first) {
                first = false;
            } else {
                y += ascent * lineHeight;
            }
            TextLayout layout = measurer.nextLayout((float) width);
            x = 0;
            if (align == Align.RIGHT) {
                x = (float) (width - layout.getAdvance());
            } else if (align == Align.CENTER) {
                x = (float) ((width - layout.getAdvance()) / 2.0F);
            } else if (align == Align.JUSTIFY) {
                // Don't justify the last line.
                if (measurer.getPosition() < text.length()) {
                    layout = layout.getJustifiedLayout((float) width);
                }
            }
            ascent = layout.getAscent();
            // y += layout.getDescent() + layout.getLeading() + layout.getAscent();
            return layout;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public void remove() {
            throw new AssertionError("This operation is not implemented");
        }
    }
}
