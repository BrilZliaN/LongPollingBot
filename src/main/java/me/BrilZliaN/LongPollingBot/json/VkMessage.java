package me.BrilZliaN.LongPollingBot.json;

public class VkMessage extends VkObject {
	
    private int id;
    private String user_id;
    private int date;
    private int read_state;
    private int out;
    private String title;
    private String body;
    private int emoji;
    private int important;
    private int deleted;

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUser_id() {
        return this.user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public int getDate() {
        return this.date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getRead_state() {
        return this.read_state;
    }

    public void setRead_state(int read_state) {
        this.read_state = read_state;
    }

    public int getOut() {
        return this.out;
    }

    public void setOut(int out) {
        this.out = out;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getEmoji() {
        return this.emoji;
    }

    public void setEmoji(int emoji) {
        this.emoji = emoji;
    }

    public int getImportant() {
        return this.important;
    }

    public void setImportant(int important) {
        this.important = important;
    }

    public int getDeleted() {
        return this.deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("VkMessage [id=");
		builder.append(id);
		builder.append(", user_id=");
		builder.append(user_id);
		builder.append(", date=");
		builder.append(date);
		builder.append(", read_state=");
		builder.append(read_state);
		builder.append(", out=");
		builder.append(out);
		builder.append(", title=");
		builder.append(title);
		builder.append(", body=");
		builder.append(body);
		builder.append(", emoji=");
		builder.append(emoji);
		builder.append(", important=");
		builder.append(important);
		builder.append(", deleted=");
		builder.append(deleted);
		builder.append("]");
		return builder.toString();
	}
}