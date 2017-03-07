package MyApp;

import obj.Message;

/**
 * Created by dries on 7/03/2017.
 */
public class CanMessage {

    Message m;
    String id;

    public CanMessage(Message m)
    {
        //String idString = String.format("%8s", Integer.toBinaryString(m.id)).replace(' ', '0');
        this.m = m;
        this.id = String.format("8%s", Integer.toHexString(m.id)).replace(' ', '0');
    }

    public Message getM() {
        return m;
    }

    public void setM(Message m) {
        this.m = m;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /*System.out.printf("%s  %d  %2d %2d %2d %2d %2d %2d %2d %2d   %d\n",
    idString, m.length, m.data[0], m.data[1], m.data[2], m.data[3], m.data[4],
    m.data[5], m.data[6], m.data[7], m.time);*/
}
