package org.example.lms.dto;

import jakarta.validation.constraints.NotBlank;

public class BookRequestDTO {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "ISBN is required")
    private String isbn;

    @NotBlank(message = "publicationYear is required")
    private String publicationYear;

    @NotBlank(message = "Author is required")
    private String author;

    public BookRequestDTO() {}

    public BookRequestDTO(String title, String isbn, String publicationYear, String author) {
        this.title = title;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getPublicationYear() {
        return publicationYear;
    }

    public String getAuthor() {
        return author;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setPublicationYear(String publicationYear) {
        this.publicationYear = publicationYear;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
