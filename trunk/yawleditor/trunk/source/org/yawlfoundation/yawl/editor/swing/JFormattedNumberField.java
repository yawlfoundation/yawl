/*
 * Created on 27/02/2002
 * Donated from Lindsay's private source library
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.swing;

import java.text.DecimalFormat;
import java.text.ParsePosition;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.NumberFormatter;

public class JFormattedNumberField extends JFormattedSelectField {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  protected double doubleValue;
  protected BoundVerifier boundVerifier;
  
  public JFormattedNumberField(String format, double value, int columns) {
    boundVerifier = new BoundVerifier();
    setInputVerifier(boundVerifier);

    setDouble(value);
    setColumns(columns);

    setFormatterFactory(new NumberFormatterFactory(format));
  }
  
  public void setDouble(double value) {
    setValue(new Double(value));
  }

  public double getDouble() {
  /*
    Some notes: there is a subtle coupling between this and BoundVerifier.
    BoundVerifier supplies an opinion on whether the current text of the
    field can be parsed, and therefore whether focus can leave.  The
    assumption is that the call to getDouble() will only occur at times
    where we know the contents of the field can be validly parsed
    (ie - after BoundVerfiier returns 'happy').

    Two items of interest that influenced this approach:
     * When FocusLost and ActionPerformed Events fire they have not yet
       updated the Double Object containing the underlying value. BUT...
       they should only fire if BoundVerifier is happy.
     * Especially for formats that round the decimal input to a displayed
       integer or number of decimal places, the field's text contains the
       actual value the user would expect to be used, not the Double value
       input.
  */
    double value;
    try {
      value = DoubleParser.toDouble(getText());
    } catch (NullPointerException npe) {
      return 0;
    }
    return value;
  }

  public void setLowerBound(double lowerBound) {
    boundVerifier.setLowerBound(lowerBound);
    if (lowerBound > getDouble()) {
      setDouble(lowerBound);
    }
  }

  public void setUpperBound(double upperBound) {
    boundVerifier.setUpperBound(upperBound);
    if (upperBound < getDouble()) {
      setDouble(upperBound);
    }
  }

  public void setBounds(double lowerBound, double upperBound) {
    setLowerBound(lowerBound);
    setUpperBound(upperBound);
  }

  class BoundVerifier extends InputVerifier {
    protected double lowerBound;
    protected double upperBound;

    public BoundVerifier() {
      super();
      setBounds(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public BoundVerifier(double lowerBound, double upperBound) {
      super();
      setBounds(lowerBound, upperBound);
    }

    public boolean verify(JComponent component) {
      assert component instanceof JFormattedNumberField;
      double value;

      JFormattedNumberField field = (JFormattedNumberField) component;

      try {
        value = DoubleParser.toDouble(field.getText());
      } catch (NullPointerException npe) {
        return false;
      }
      return verifyBounds(value);
    }

    protected boolean verifyBounds(double number) {
      if (number < lowerBound) {
        return false;
      }
      if (number > upperBound) {
        return false;
      }
      return true;
    }

    public boolean shouldYieldFocus(JComponent component) {
      boolean isValid = verify(component);
      if (!isValid) {
        JFormattedNumberField field = (JFormattedNumberField) component;
        field.invalidEdit();
      }
      return isValid;
    }

    public void setLowerBound(double lowerBound) {
      this.lowerBound = lowerBound;
    }

    public void setUpperBound(double upperBound) {
      this.upperBound = upperBound;
    }

    public void setBounds(double lowerBound, double upperBound) {
      setLowerBound(lowerBound);
      setUpperBound(upperBound);
    }
  }
}

class NumberFormatterFactory extends JFormattedTextField.AbstractFormatterFactory {
  private String format;
  
  private static final String DIGITS = "0123456789";
  
  public NumberFormatterFactory(String format) {
    this.format = format;
  }
  
  public AbstractFormatter getFormatter(JFormattedTextField field) {
    assert field instanceof JFormattedNumberField;

    if (isPercentFormat()) {
      return getPercentFormatter(); 
    }
    if (isDecimalFormat()) {
      return getDecimalFormatter();
    }
    if(isIntegerFormat()) {
      return getIntegerFormatter();
    }
    return null;
  }
  
  private GenericNumberFormatter getPercentFormatter() {
    return new GenericNumberFormatter(negativeCharacterIfRequired() + DIGITS + ".%");
  }

  private GenericNumberFormatter getDecimalFormatter() {
    return new GenericNumberFormatter(negativeCharacterIfRequired() + DIGITS + ".");
  }
  
  private GenericNumberFormatter getIntegerFormatter() {
    return new GenericNumberFormatter(negativeCharacterIfRequired() + DIGITS);
  }

  private boolean isPercentFormat() {
    String trimmedFormat = format.trim();
    if (trimmedFormat.charAt(trimmedFormat.length() - 1) == '%') {
      return true;
    }
    return false;
  }

  private boolean isDecimalFormat() {
    String trimmedFormat = format.trim();
    if (trimmedFormat.indexOf('.') != -1) {
      return true;
    }
    return false;
  }
  
  private boolean isIntegerFormat() {
    return (!isDecimalFormat() && !isPercentFormat());  
  }
  
  private String negativeCharacterIfRequired() {
    if (isNegativeNumberFormat()) {
      return "-";
    } 
    return null;
  }
  
  private boolean isNegativeNumberFormat() {
    String trimmedFormat = format.trim();
    if (trimmedFormat.charAt(0) == '-') {
      return true;
    }
    return false;
  }
}

class GenericNumberFormatter extends NumberFormatter {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private GenericNumberFilter filter = new GenericNumberFilter();

  public GenericNumberFormatter() {
    super(new DecimalFormat());
  }
  
  public GenericNumberFormatter(String validCharacters) {
    super(new DecimalFormat());
    setValidCharacters(validCharacters);    
  }

  protected DocumentFilter getDocumentFilter() {
    return filter;
  }
  
  public void setValidCharacters(String validCharacters) {
    filter.setValidCharacters(validCharacters);
  }
}

class GenericNumberFilter extends DocumentFilter {
  private String validCharacters;
  
  public GenericNumberFilter() {
    this.validCharacters = "0123456789";
  }
  
  public GenericNumberFilter(String validCharacters) {
    this.validCharacters = validCharacters;
  }
  
  public void setValidCharacters(String validCharacters) {
    this.validCharacters = validCharacters;
  }

  public void replace(DocumentFilter.FilterBypass bypass,
                      int offset,
                      int length,
                      String text,
                      AttributeSet attributes) throws BadLocationException {
    if (isValidText(text))
      super.replace(bypass,offset,length,text,attributes);
  }

  protected boolean isValidText(String text) {
    for (int i = 0; i < text.length(); i++) {
      if (validCharacters.indexOf(text.charAt(i)) == -1) {
        return false;
      }
    }
    return true;
  }
}

class DoubleParser {
  
  private static final DecimalFormat FORMAT = new DecimalFormat();
/*
  This is a down and dirty hack around DecimalFormat to correctly parse
  perentage strings to their double equivalent + the standard parsing for
  double strings that typically works very well. Left to its own devices
  DecimalFormat.parse() ignores the percentage sign, effectively
  multiplying the number returned by 100.
*/

  public static double toDouble(String text) throws NullPointerException {
    String trimmedText = text.trim();

    if (trimmedText.length() == 0) {
      throw new NullPointerException();
    }

    boolean isPercentString = false;

    if (trimmedText.charAt(trimmedText.length() - 1) == '%') {
      isPercentString = true;
      trimmedText = trimmedText.replace('%',' ');
      trimmedText = trimmedText.trim();
    }

    double value = (FORMAT.parse(
                      trimmedText, 
                      new ParsePosition(0))
                   ).doubleValue();

    if (isPercentString) {
      value = value / 100;
    }
    return value;
  }
}
