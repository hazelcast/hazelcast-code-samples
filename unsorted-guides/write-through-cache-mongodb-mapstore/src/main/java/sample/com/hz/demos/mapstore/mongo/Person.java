package sample.com.hz.demos.mapstore.mongo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
* Snippets of this code are included as examples in our documentation,
* using the tag:: comments.
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// tag::class[]
public class Person implements Serializable {

    private Integer id;

    private String name;

    private String lastname;

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}
// end::class[]
