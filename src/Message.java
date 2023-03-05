public class Message {
    public final int opcode;
    public ShareInstance share=null;
    public String string=null;
    public Message(int o){
        this.opcode=o;
    }
}
