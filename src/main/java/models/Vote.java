package models;

import java.util.Date;

public class Vote {
    private int id;
    private String type; // 'like' ou 'dislike'
    private Date created_at;
    private int user_id;
    private Integer sujet_id;
    private Integer commentaire_id;

    public Vote() {}

    public Vote(String type, int user_id, Integer sujet_id, Integer commentaire_id) {
        this.type = type;
        this.user_id = user_id;
        this.sujet_id = sujet_id;
        this.commentaire_id = commentaire_id;
        this.created_at = new Date();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Date getCreated_at() { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }

    public int getUser_id() { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }

    public Integer getSujet_id() { return sujet_id; }
    public void setSujet_id(Integer sujet_id) { this.sujet_id = sujet_id; }

    public Integer getCommentaire_id() { return commentaire_id; }
    public void setCommentaire_id(Integer commentaire_id) { this.commentaire_id = commentaire_id; }
}