/**
 * Container for a message, with extra information.
 *
 * Using a frame, we can send messages that contain information about the type of message it is and the user who sent the message.
 */
public class Frame {

    public final int tag;
    public final String username;
    public final byte[] data;

    // cria um frame com a informacao especificada,
    // tipo de frame, quem mandou o frame, mensagem
    public Frame(int tag, String username, byte[] data) {
        this.tag = tag;
        this.username = username;
        this.data = data;
    }
}