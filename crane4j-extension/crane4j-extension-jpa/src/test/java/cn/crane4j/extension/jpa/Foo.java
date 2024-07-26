package cn.crane4j.extension.jpa;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author huangchengxing
 */
@Table(name = "foo")
@Entity
@Getter
@Setter
public class Foo {
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String userName;

    @Column(name = "age")
    private Integer userAge;

    @Column(name = "sex")
    private Integer userSex;
}
