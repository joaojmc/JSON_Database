package server;

public class Response {
    String response;
    Object value;
    String reason;

    @Override
    public String toString() {
        return "Response{" +
                "response='" + response + '\'' +
                ", value='" + (value != null ? value : null) + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}
