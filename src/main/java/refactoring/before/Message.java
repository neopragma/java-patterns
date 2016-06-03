package refactoring.before;

public class Message {
	
	private String destination;
	private Object payload;
	
	public Message(String destination, Object payload) {
		this.destination = destination;
		this.payload = payload;
	}

	public String getDestination() {
		return destination;
	}

	public Object getPayload() {
		return payload;
	}

}
