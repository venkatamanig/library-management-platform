package org.example.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class BookUpdateDTO {

    @Pattern(regexp = ".*\\S.*", message = "title cannot be blank")
    @Size(max = 255, message = "title cannot exceed 255 characters")
    private String title;

    @Pattern(regexp = ".*\\S.*", message = "Author cannot be blank")
    @Size(max = 255, message = "Author cannot exceed 255 characters")
    private String author;

    @Pattern(regexp = ".*\\S.*", message = "ISBN cannot be blank")
    private String isbn;

    @Pattern(regexp = ".*\\S.*", message = "publicationYear cannot be blank")
    private String publicationYear;



    public BookUpdateDTO() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(String publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
