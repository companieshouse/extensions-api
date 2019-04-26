package uk.gov.companieshouse.extensions.api.requests;

import java.util.Objects;
import java.util.Optional;

public class CreatedBy {
    private String id;
    private String forename;
    private String surname;
    private String email;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreatedBy createdBy = (CreatedBy) o;
        return Objects.equals(id, createdBy.id) &&
            Objects.equals(forename, createdBy.forename) &&
            Objects.equals(surname, createdBy.surname) &&
            Objects.equals(email, createdBy.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, forename, surname, email);
    }
}
