package signature;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.JPanel;


public class Canvas extends JPanel
    implements MouseMotionListener, MouseListener
{

    public Canvas()
    {
        //setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
    	size = new Dimension(0, 0);
        v = new Vector(15, 10);
        ImgName = new String[12];
        _$11796 = true;
        _$11797 = false;
        _$11798 = true;
        _$11800 = false;
        _$11801 = false;
        _$11802 = false;
        Text = "";
        img = new Image[11];
        bButtonRight = false;
        iColor = 0;
        fgColor = 0;
        bgColor = -1;
        iAction = 24;//24
        iSize = 2;
        _$11814 = 0;
        iNB = 0;
        sNB = "";
        addMouseMotionListener(this);
        addMouseListener(this);
        for(int i = 0; i < 11; i++)
            img[i] = null;

    }

    public void update(Graphics g)
    {
        paint(g);
    }

    public void init()
    {
        size = getSize();
        iW = size.width;
        iH = size.height;
        image = createImage(size.width, size.height);
        image_graphics = image.getGraphics();
        pixels = new int[size.width * size.height];
        v.addElement(String.valueOf(String.valueOf((new StringBuffer("32 ; ")).append(bgColor).append(" ;"))));
        v.addElement(String.valueOf(String.valueOf((new StringBuffer("6 ; 0 ; 0 ; ")).append(size.width).append(" ; ").append(size.height).append(" ;"))));
        v.addElement(String.valueOf(String.valueOf((new StringBuffer("32 ; ")).append(fgColor).append(" ;"))));
    }

    public void triangle(int i, int j, int k, int l)
    {
        int ai[] = {
            i, i + k / 2, i + k
        };
        int ai1[] = {
            j + l, j, j + l
        };
        image_graphics.fillPolygon(ai, ai1, 3);
    }

    public void setBkImage(Image image, String s, int i)
    {
        if(i < 11)
        {
            img[i] = image;
            ImgName[i] = s;
        }
    }

    public void paintCanvas()
    {
        if(_$11800)
        {
            image_graphics.copyArea(size.width, 0, size.width, size.height, -size.width, 0);
            image_graphics.fillRect(0, 0, size.width * 2, size.height);
            doRedraw(image_graphics);
            _$11800 = false;
            image_graphics.copyArea(0, 0, size.width, size.height, size.width, 0);
            _$11796 = true;
            return;
        }
        if(!_$11797 && _$11796)
            image_graphics.copyArea(size.width, 0, size.width, size.height, -size.width, 0);
        if(_$11798)
        {
            image_graphics.setColor(new Color(bgColor));
            image_graphics.fillRect(0, 0, size.width * 2, size.height);
            if(img[0] != null)
            {
                image_graphics.drawImage(img[0], 0, 0, this);
                image_graphics.drawImage(img[0], size.width, 0, this);
                v.addElement(" 22 ; 0 ; 0 ; 0 ;");
            }
            _$11798 = false;
        }
        int i = 0;
        if(bButtonRight || iAction == 25)
            i = bgColor;
        else
            i = fgColor;
        image_graphics.setColor(new Color(i));
        if(iColor != i)
        {
            iColor = i;
            v.addElement(String.valueOf(String.valueOf((new StringBuffer("32 ; ")).append(iColor).append(" ;"))));
        }
        if(_$11802)
            switch(iAction)
            {
            default:
                break;

            case 1: // '\001'
                _$11797 = false;
                drawRect(image_graphics, iSize, x1, y1, x2, y2, true);
                break;

            case 6: // '\006'
                _$11797 = false;
                image_graphics.fillRect(x1, y1, x2 - x1, y2 - y1);
                image_graphics.fillRect(x2, y2, x1 - x2, y1 - y2);
                image_graphics.fillRect(x2, y1, x1 - x2, y2 - y1);
                image_graphics.fillRect(x1, y2, x2 - x1, y1 - y2);
                break;

            case 4: // '\004'
                _$11797 = false;
                drawOval(image_graphics, iSize, x1, y1, x2, y2, true);
                break;

            case 9: // '\t'
                _$11797 = false;
                image_graphics.fillOval(x1, y1, x2 - x1, y2 - y1);
                image_graphics.fillOval(x2, y2, x1 - x2, y1 - y2);
                image_graphics.fillOval(x2, y1, x1 - x2, y2 - y1);
                image_graphics.fillOval(x1, y2, x2 - x1, y1 - y2);
                break;

            case 21: // '\025'
                _$11797 = false;
                _$8350(iSize, x1, y1, x2, y2, _$11814);
                break;

            case 24: // '\030'
            case 25: // '\031'
                _$11797 = true;
                if(iAction == 25)
                    _$8350(3, old_x, old_y, new_x, new_y);
                else
                    _$8350(iSize, old_x, old_y, new_x, new_y);
                iNB++;
                sNB = String.valueOf(String.valueOf((new StringBuffer(String.valueOf(String.valueOf(sNB)))).append(new_x).append(";").append(new_y).append(";")));
                if(iNB > 30)
                {
                    if(iAction == 25)
                        v.addElement(String.valueOf(String.valueOf((new StringBuffer("24 ; 3 ; ")).append(sNB))));
                    else
                        v.addElement(String.valueOf(String.valueOf((new StringBuffer("24 ; ")).append(iSize).append(" ; ").append(sNB))));
                    iNB = 1;
                    sNB = String.valueOf(String.valueOf((new StringBuffer("")).append(new_x).append(";").append(new_y).append(";")));
                }
                old_x = new_x;
                old_y = new_y;
                break;

            case 11: // '\013'
                _$11797 = false;
                fillZone(x2, y2, fgColor);
                break;

            case 20: // '\024'
                image_graphics.setClip(0, 0, size.width, size.height);
                _$11797 = false;
                Text = _$11832();
                image_graphics.drawString(Text, x2, y2);
                image_graphics.setClip(0, 0, size.width * 2, size.height);
                break;

            case 950: 
            case 951: 
            case 952: 
            case 953: 
            case 954: 
            case 955: 
            case 956: 
            case 957: 
            case 958: 
            case 959: 
                int j = (iAction - 950) + 1;
                _$11797 = false;
                if(img[j] != null)
                {
                    image_graphics.setClip(0, 0, size.width, size.height);
                    image_graphics.drawImage(img[j], x2, y2, this);
                    image_graphics.setClip(0, 0, size.width * 2, size.height);
                }
                break;
            }
    }

    private void _$8350(int i, int j, int k, int l, int i1)
    {
        _$8350(i, j, k, l, i1, 0);
    }

    private void _$8350(int i, int j, int k, int l, int i1, int j1)
    {
        drawLine(image_graphics, i, j, k, l, i1, j1);
    }

    static void drawLine(Graphics g, int i, int j, int k, int l, int i1, int j1)
    {
        int k1 = 0;
        int l1 = 0;
        int i2 = 0;
        int j2 = 0;
        if(j1 > 0)
        {
            int ai[] = {
                j, 0, 0
            };
            int ai1[] = {
                k, 0, 0
            };
            int l2 = j - l;
            int i3 = k - i1;
            k1 = l - j - i3 / 2;
            i2 = (i1 - k) + l2 / 2;
            int j3 = 8 + i * i;
            double d = Math.sqrt((k1 * k1 + i2 * i2) / (j3 * j3));
            if(d == (double)0)
                d = 1.0D;
            if(j1 == 1 || j1 == 3)
            {
                ai[1] = j + (int)((double)(l - j - i3 / 2) / d);
                ai1[1] = k + (int)((double)((i1 - k) + l2 / 2) / d);
                ai[2] = j + (int)((double)((l - j) + i3 / 2) / d);
                ai1[2] = k + (int)((double)(i1 - k - l2 / 2) / d);
                g.fillPolygon(ai, ai1, 3);
                k1 = ai[2] - (ai[2] - ai[1]) / 2;
                i2 = ai1[2] - (ai1[2] - ai1[1]) / 2;
                if(j1 == 1)
                {
                    l1 = l;
                    j2 = i1;
                }
            }
            if(j1 == 2 || j1 == 3)
            {
                ai[0] = l;
                ai1[0] = i1;
                ai[1] = l + (int)((double)(j - l - i3 / 2) / d);
                ai1[1] = i1 + (int)((double)((k - i1) + l2 / 2) / d);
                ai[2] = l + (int)((double)((j - l) + i3 / 2) / d);
                ai1[2] = i1 + (int)((double)(k - i1 - l2 / 2) / d);
                g.fillPolygon(ai, ai1, 3);
                if(j1 == 2)
                {
                    k1 = j;
                    i2 = k;
                }
                l1 = ai[2] - (ai[2] - ai[1]) / 2;
                j2 = ai1[2] - (ai1[2] - ai1[1]) / 2;
            }
        }
        if(j1 != 0)
        {
            j = k1;
            l = l1;
            k = i2;
            i1 = j2;
        }
        g.drawLine(j, k, l, i1);
        for(int k2 = 1; k2 < i; k2++)
        {
            g.drawLine(j - k2, k, l - k2, i1);
            g.drawLine(j + k2, k, l + k2, i1);
            g.drawLine(j, k - k2, l, i1 - k2);
            g.drawLine(j, k + k2, l, i1 + k2);
        }

    }

    static void drawRect(Graphics g, int i, int j, int k, int l, int i1, boolean flag)
    {
        int j1 = j;
        int k1 = l;
        int l1 = k;
        int i2 = i1;
        if(flag)
        {
            k1 = l - j;
            i2 = i1 - k;
            if(l <= j)
            {
                k1 = j - l;
                j1 = l;
            }
            if(i1 <= k)
            {
                i2 = k - i1;
                l1 = i1;
            }
        }
        for(int j2 = 0; j2 < i; j2++)
            g.drawRect(j1 + j2, l1 + j2, k1 - j2 * 2, i2 - j2 * 2);

    }

    static void drawOval(Graphics g, int i, int j, int k, int l, int i1, boolean flag)
    {
        int j1 = j;
        int k1 = l;
        int l1 = k;
        int i2 = i1;
        if(flag)
        {
            k1 = l - j;
            i2 = i1 - k;
            if(l <= j)
            {
                k1 = j - l;
                j1 = l;
            }
            if(i1 <= k)
            {
                i2 = k - i1;
                l1 = i1;
            }
        }
        for(int j2 = 0; j2 < i; j2++)
            g.drawOval(j1 + j2, l1 + j2, k1 - j2 * 2, i2 - j2 * 2);

    }

    public void mousePressed(MouseEvent mouseevent)
    {
        x1 = mouseevent.getX();
        y1 = mouseevent.getY();
        bButtonRight = mouseevent.getModifiers() == 4 || mouseevent.getModifiers() == 1;
        x2 = x1 + 1;
        y2 = y1 + 1;
        iNB = 1;
        sNB = String.valueOf(String.valueOf((new StringBuffer("")).append(x1).append(";").append(y1).append(";")));
        old_x = new_x = x1;
        old_y = new_y = y1;
        _$11802 = true;
        repaint();
    }

    public void mouseReleased(MouseEvent mouseevent)
    {
        int i = mouseevent.getX();
        int j = mouseevent.getY();
        if(_$11801)
        {
            if(iAction != 24)
            {
                if(iAction >= 950 && iAction < 960)
                    v.addElement(String.valueOf(String.valueOf((new StringBuffer("22 ; ")).append((iAction + 1) - 950).append(" ; ").append(x2).append(" ; ").append(y2).append(" ;"))));
                else
                if(iAction == 20)
                    v.addElement(String.valueOf(String.valueOf((new StringBuffer("")).append(iAction).append(" ; ").append(_$11832()).append(" ; ").append(x2).append(" ; ").append(y2).append(" ;"))));
                else
                if(iAction == 20)
                    v.addElement(String.valueOf(String.valueOf((new StringBuffer("")).append(iAction).append(" ; ").append(x1).append(" ; ").append(y1).append(" ; ").append(x2).append(" ; ").append(y2).append(" ;"))));
                else
                if(iAction == 21)
                    v.addElement(String.valueOf(String.valueOf((new StringBuffer("")).append(iAction).append(" ; ").append(iSize).append(" ; ").append(x1).append(" ; ").append(y1).append(" ; ").append(x2).append(" ; ").append(y2).append(" ;").append(_$11814).append(" ;"))));
                else
                if(iAction == 25)
                    v.addElement(String.valueOf(String.valueOf((new StringBuffer("24 ; 3 ; ")).append(sNB))));
                else
                if(iAction == 4 || iAction == 9 || iAction == 1 || iAction == 6)
                {
                    int k = x1;
                    int l = x2 - x1;
                    int i1 = y1;
                    int j1 = y2 - y1;
                    if(x2 <= x1)
                    {
                        l = x1 - x2;
                        k = x2;
                    }
                    if(y2 <= y1)
                    {
                        j1 = y1 - y2;
                        i1 = y2;
                    }
                    if(iAction == 4 || iAction == 1)
                        v.addElement(String.valueOf(String.valueOf((new StringBuffer("")).append(iAction).append(" ; ").append(iSize).append(" ; ").append(k).append(" ; ").append(i1).append(" ; ").append(l).append(" ; ").append(j1).append(" ;"))));
                    else
                        v.addElement(String.valueOf(String.valueOf((new StringBuffer("")).append(iAction).append(" ; ").append(k).append(" ; ").append(i1).append(" ; ").append(l).append(" ; ").append(j1).append(" ;"))));
                } else
                {
                    v.addElement(String.valueOf(String.valueOf((new StringBuffer("")).append(iAction).append(" ; ").append(x1).append(" ; ").append(y1).append(" ; ").append(x2).append(" ; ").append(y2).append(" ;"))));
                }
            } else
            {
                v.addElement(String.valueOf(String.valueOf((new StringBuffer("24 ; ")).append(iSize).append(" ; ").append(sNB))));
            }
            image_graphics.copyArea(0, 0, size.width, size.height, size.width, 0);
            _$11796 = true;
        }
        _$11798 = false;
        _$11802 = false;
        repaint();
    }

    public void mouseDragged(MouseEvent mouseevent)
    {
        int i = mouseevent.getX();
        int j = mouseevent.getY();
        if(_$11801 && _$11802)
        {
            if(i > 0 && i < iW)
                x2 = i;
            if(j > 0 && j < iH)
                y2 = j;
            new_x = i;
            new_y = j;
            repaint();
        }
    }

    public void mouseMoved(MouseEvent mouseevent)
    {
    }

    public void mouseClicked(MouseEvent mouseevent)
    {
    }

    public void mouseEntered(MouseEvent mouseevent)
    {
        _$11801 = true;
    }

    public void mouseExited(MouseEvent mouseevent)
    {
        _$11801 = false;
        repaint();
    }

    public void paint(Graphics g)
    {
        if(image == null)
            init();
        paintCanvas();
        g.drawImage(image, 0, 0, this);
    }

    private String _$11832()
    {
		return Text;
//        if(zz != null)
//            //return zz.getText();
//        else
//            return "";
    }

    public void onErase()
    {
        getData();
        v.removeAllElements();
        v.addElement(String.valueOf(String.valueOf((new StringBuffer("32 ; ")).append(bgColor).append(" ;"))));
        v.addElement(String.valueOf(String.valueOf((new StringBuffer("6 ; 0 ; 0 ; ")).append(size.width).append(" ; ").append(size.height).append(" ;"))));
        v.addElement(String.valueOf(String.valueOf((new StringBuffer("32 ; ")).append(fgColor).append(" ;"))));
        _$11798 = true;
        setBkImage(null, "", 0);//deletes the background image if there is any
        repaint();
    }

    public void onUndo()
    {
        _$11800 = true;
        repaint();
    }

    public void onColor(int i, int j)
    {
        fgColor = i;
        bgColor = j;
    }

    public void onAction(int i)
    {
        iAction = i;
    }

    public void onSize(int i)
    {
        iSize = i;
    }

    public void onArrow(int i)
    {
        _$11814 = i;
    }

    public void onFont(Font font)
    {
        if(font != null)
        {
            image_graphics.setFont(font);
            v.addElement(String.valueOf(String.valueOf((new StringBuffer("31 ; ")).append(font.getName()).append(" ; ").append(font.getStyle()).append(" ; ").append(font.getSize()).append(" ;"))));
        }
    }

    public String getData()
    {
        String s = "";
        for(Enumeration enumeration = v.elements(); enumeration.hasMoreElements();)
            s = String.valueOf(String.valueOf((new StringBuffer(String.valueOf(String.valueOf(s)))).append(enumeration.nextElement().toString()).append("\n")));

        return s;
    }

    public void doRedraw(Graphics g)
    {
        Enumeration enumeration = v.elements();
        int i = v.size();
        v.removeElementAt(i - 1);
        Interpol z_data1;

    }

    protected void fillZone(int i, int j)
    {
        fillZone(i, j, fgColor);
    }

    protected void fillZone(int i, int j, int k)
    {
        PixelGrabber pixelgrabber = new PixelGrabber(image, 0, 0, size.width, size.height, pixels, 0, size.width);
        try
        {
            pixelgrabber.grabPixels();
        }
        catch(InterruptedException interruptedexception)
        {
            System.out.println("--> erreur fillZone 1 : ".concat(String.valueOf(String.valueOf(interruptedexception))));
        }
        if(k > 0)
            k -= 0x1000000;
        int l = pixels[j * size.width + i];
        if(k != l)
        {
            _$11863(i, j, k, l);
            try
            {
                Image image = createImage(new MemoryImageSource(size.width, size.height, new DirectColorModel(24, 0xff0000, 65280, 255), pixels, 0, size.width));
                image_graphics.drawImage(image, 0, 0, this);
            }
            catch(Exception exception)
            {
                System.out.println("--> erreur fillZone 2 : ".concat(String.valueOf(String.valueOf(exception))));
            }
        }
    }

    private void _$11866(int i, int j, int k, int l)
    {
        pixels[j * iW + i] = k;
        if(i > 0 && l == pixels[j * iW + (i - 1)])
            _$11866(i - 1, j, k, l);
        if(i < iW - 1 && l == pixels[j * iW + (i + 1)])
            _$11866(i + 1, j, k, l);
        if(j < iH - 1 && l == pixels[(j + 1) * iW + i])
            _$11866(i, j + 1, k, l);
        if(j > 0 && l == pixels[(j - 1) * iW + i])
            _$11866(i, j - 1, k, l);
    }

    private void _$11863(int i, int j, int k, int l)
    {
        Stack stack = new Stack();
        Point point = new Point(i, j);
        stack.push(point);
        do
        {
            if(stack.isEmpty())
                break;
            Point point1;
            for(point1 = (Point)stack.pop(); _$11875(point1.x, point1.y) == k && !stack.isEmpty(); point1 = (Point)stack.pop());
            int k1 = point1.x;
            int l1 = point1.x;
            int i1 = point1.x;
            int j1 = point1.y;
            _$11876(i1, j1, k);
            i1++;
            for(int i2 = _$11875(i1, j1); (i2 == l || i2 == k) && i1 < iW; i2 = _$11875(i1, j1))
            {
                if(i2 != k)
                    _$11876(i1, j1, k);
                l1 = i1;
                i1++;
            }

            i1 = point1.x - 1;
            int j2 = _$11875(i1, j1);
            do
            {
                if(j2 != l && j2 != k || i1 <= -1)
                    break;
                if(j2 != k)
                    _$11876(i1, j1, k);
                k1 = i1;
                if(--i1 < 0)
                    break;
                j2 = _$11875(i1, j1);
            } while(true);
            i1 = l1;
            if(j1 < iH)
                while(i1 >= k1) 
                {
                    int k2;
                    for(k2 = _$11875(i1, j1 + 1); k2 != l && i1 >= k1; k2 = _$11875(i1, j1 + 1))
                        i1--;

                    if(i1 >= k1 && k2 == l)
                        stack.push(new Point(i1, j1 + 1));
                    k2 = _$11875(i1, j1 + 1);
                    while((k2 == l || k2 == k) && i1 >= k1) 
                    {
                        i1--;
                        k2 = _$11875(i1, j1 + 1);
                    }
                }
            i1 = l1;
            if(j1 > 0)
                while(i1 >= k1) 
                {
                    int l2;
                    for(l2 = _$11875(i1, j1 - 1); l2 != l && i1 >= k1; l2 = _$11875(i1, j1 - 1))
                        i1--;

                    if(i1 >= k1 && l2 == l)
                        stack.push(new Point(i1, j1 - 1));
                    l2 = _$11875(i1, j1 - 1);
                    while((l2 == l || l2 == k) && i1 >= k1) 
                    {
                        i1--;
                        l2 = _$11875(i1, j1 - 1);
                    }
                }
        } while(true);
    }

    private int _$11875(int i, int j)
    {
        if(i < iW && j < iH && i > -1 && j > -1)
            return pixels[j * iW + i];
        else
            return -1;
    }

    private void _$11876(int i, int j, int k)
    {
        if(i < iW && j < iH && i > -1 && j > -1)
            pixels[j * iW + i] = k;
    }

    Dimension size;
    int pixels[];
    int x1;
    int x2;
    int y1;
    int y2;
    int X;
    int iW;
    int iH;
    public Vector v;
    String ImgName[];
    int old_x;
    int old_y;
    int new_x;
    int new_y;
    private boolean _$11796;
    private boolean _$11797;
    private boolean _$11798;
    private boolean _$11800;
    private boolean _$11801;
    private boolean _$11802;
    String Text;
    protected Image img[];
    Image image;
    private Graphics image_graphics;
    private Graphics _$11806;
    boolean bButtonRight;
    int iColor;
    int fgColor;
    int bgColor;
    int iAction;
    int iSize;
    private int _$11814;
    int iNB;
    String sNB;
}