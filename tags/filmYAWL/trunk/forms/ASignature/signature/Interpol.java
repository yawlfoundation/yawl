package signature;


import java.awt.*;

public class Interpol
{

    public Interpol()
    {
        iEpaisseur = 1;
        iAction = -1;
        r = new int[60];
        rr = new int[60];
        iL = -1;
        valid = false;
    }

    public Interpol(String s1, int i, int ai[])
    {
        iEpaisseur = 1;
        iAction = -1;
        r = new int[60];
        rr = new int[60];
        iL = -1;
        valid = false;
        set(s1, i, ai);
        valid = true;
    }

    public Interpol(String s1)
    {
        iEpaisseur = 1;
        iAction = -1;
        r = new int[60];
        rr = new int[60];
        iL = -1;
        valid = false;
        byte byte0 = 59;
        boolean flag = false;
        int j = 0;
        int k = 0;
        int l = 0;
        int ai[] = new int[70];
        int i1 = 0;
        if(s1 == null)
        {
            valid = false;
            return;
        }
        do
        {
            int i = s1.indexOf(byte0, i1);
            String s2;
            if(i > 0)
                s2 = new String(s1.substring(i1, i).trim());
            else
                s2 = new String(s1.substring(i1).trim());
            boolean flag1;
            try
            {
                l = Integer.parseInt(s2);
                if(iAction == 20 && k == 1)
                    flag1 = false;
                else
                    flag1 = true;
            }
            catch(NumberFormatException numberformatexception)
            {
                flag1 = false;
            }
            if(i > -1 || s2.length() > 0)
            {
                if(flag1)
                {
                    if(k == 0)
                        iAction = l;
                    else
                    if(j < 70)
                        ai[j++] = l;
                } else
                {
                    s = new String(s2);
                }
                k++;
            }
            if(i == -1)
                break;
            i1 = i + 1;
        } while(true);
        for(int k1 = 0; k1 < j; k1++)
            if(iAction == 24)
            {
                if(k1 == 0)
                {
                    iEpaisseur = ai[0];
                    continue;
                }
                int j1 = (k1 - 1) / 2;
                if(k1 % 2 != 0)
                    r[j1] = ai[k1];
                else
                    rr[j1] = ai[k1];
            } else
            {
                r[k1] = ai[k1];
            }

        iL = j;
        valid = k > 1;
    }

    public boolean isValid()
    {
        return valid;
    }

    public void set(String s1, int i, int ai[])
    {
        s = s1;
        iAction = i;
        r = ai;
        if(r != null)
            iL = ai.length;
    }

    public String toString()
    {
        String s1 = String.valueOf(String.valueOf((new StringBuffer("")).append(iAction).append(" ; ")));
        if(s != null && s.length() > 0)
            s1 = String.valueOf(String.valueOf((new StringBuffer(String.valueOf(String.valueOf(s1)))).append(s).append(" ; ")));
        if(r != null)
        {
            for(int i = 0; i < r.length; i++)
                s1 = String.valueOf(String.valueOf((new StringBuffer(String.valueOf(String.valueOf(s1)))).append(r[i]).append(" ; ")));

        }
        return s1;
    }

    public void paint(Canvas cdraw, Graphics g)
    {
label0:
        switch(iAction)
        {
        case 12: // '\f'
        case 13: // '\r'
        case 14: // '\016'
        case 15: // '\017'
        case 16: // '\020'
        case 17: // '\021'
        case 18: // '\022'
        case 19: // '\023'
        case 23: // '\027'
        case 25: // '\031'
        case 26: // '\032'
        case 27: // '\033'
        case 28: // '\034'
        case 29: // '\035'
        case 30: // '\036'
        default:
            break;

        case 1: // '\001'
            if(iL > 3)
                //cDraw.drawRect(g, r[0], r[1], r[2], r[3], r[4], false);
            break;

        case 2: // '\002'
            if(iL > 4)
            {
                g.draw3DRect(r[0], r[1], r[2], r[3], r[4] == 0);
                break;
            }
            if(iL > 3)
                g.draw3DRect(r[0], r[1], r[2], r[3], false);
            break;

        case 3: // '\003'
            if(iL > 5)
                g.drawRoundRect(r[0], r[1], r[2], r[3], r[4], r[5]);
            break;

        case 4: // '\004'
            if(iL > 4)
                //cDraw.drawOval(g, r[0], r[1], r[2], r[3], r[4], false);
            break;

        case 5: // '\005'
            if(iL > 5)
                g.drawArc(r[0], r[1], r[2], r[3], r[4], r[5]);
            break;

        case 24: // '\030'
            if(iL <= 2)
                break;
            g.drawPolyline(r, rr, (iL - 1) / 2);
            if(iEpaisseur <= 1)
                break;
            int i = 1;
            do
            {
                if(i >= iEpaisseur)
                    break label0;
                for(int j = 0; j < (iL - 1) / 2; j++)
                    r[j] = r[j] - i;

                g.drawPolyline(r, rr, (iL - 1) / 2);
                for(int k = 0; k < (iL - 1) / 2; k++)
                    r[k] = r[k] + i + i;

                g.drawPolyline(r, rr, (iL - 1) / 2);
                for(int l = 0; l < (iL - 1) / 2; l++)
                {
                    r[l] = r[l] - i;
                    rr[l] = rr[l] - i;
                }

                g.drawPolyline(r, rr, (iL - 1) / 2);
                for(int i1 = 0; i1 < (iL - 1) / 2; i1++)
                    rr[i1] = rr[i1] + i + i;

                g.drawPolyline(r, rr, (iL - 1) / 2);
                for(int j1 = 0; j1 < (iL - 1) / 2; j1++)
                    rr[j1] = rr[j1] - i;

                i++;
            } while(true);

        case 6: // '\006'
            if(iL > 3)
                g.fillRect(r[0], r[1], r[2], r[3]);
            break;

        case 7: // '\007'
            if(iL > 4)
            {
                g.fill3DRect(r[0], r[1], r[2], r[3], r[4] == 0);
                break;
            }
            if(iL > 3)
                g.fill3DRect(r[0], r[1], r[2], r[3], false);
            break;

        case 8: // '\b'
            if(iL > 5)
                g.fillRoundRect(r[0], r[1], r[2], r[3], r[4], r[5]);
            break;

        case 9: // '\t'
            if(iL > 3)
                g.fillOval(r[0], r[1], r[2], r[3]);
            break;

        case 10: // '\n'
            if(iL > 5)
                g.fillArc(r[0], r[1], r[2], r[3], r[4], r[5]);
            break;

        case 11: // '\013'
            if(iL > 2)
                cdraw.fillZone(r[0], r[1]);
            break;

        case 20: // '\024'
            if(iL > 1)
                g.drawString(s, r[0], r[1]);
            break;

        case 21: // '\025'
            if(iL > 3)
                Canvas.drawLine(g, r[0], r[1], r[2], r[3], r[4], r[5]);
            break;

        case 22: // '\026'
            if(iL <= 2 || cdraw.img[r[0]] == null)
                break;
            if(iL > 4)
                g.drawImage(cdraw.img[r[0]], r[1], r[2], r[3], r[4], null);
            else
                g.drawImage(cdraw.img[r[0]], r[1], r[2], null);
            break;

        case 31: // '\037'
            if(iL > 1)
                g.setFont(new Font(s, r[0], r[1]));
            break;

        case 32: // ' '
            if(iL > 0)
            {
                g.setColor(new Color(r[0]));
                cdraw.fgColor = r[0];
            }
            break;
        }
    }

    public static final int drawRect = 1;
    public static final int draw3DRect = 2;
    public static final int drawRoundRect = 3;
    public static final int drawOval = 4;
    public static final int drawArc = 5;
    public static final int fillRect = 6;
    public static final int fill3DRect = 7;
    public static final int fillRoundRect = 8;
    public static final int fillOval = 9;
    public static final int fillArc = 10;
    public static final int fillZne = 11;
    public static final int drawString = 20;
    public static final int drawLine = 21;
    public static final int drawImage = 22;
    public static final int drawImagePart = 23;
    public static final int freehand = 24;
    public static final int erase = 25;
    public static final int bak = 26;
    public static final int loadImage = 30;
    public static final int setFont = 31;
    public static final int setColor = 32;
    public static final int changeClr = 33;
    public static final int symbol = 950;
    public static final int clear = 999;
    int iEpaisseur;
    int iAction;
    int r[];
    int rr[];
    String s;
    int iL;
    boolean valid;

}