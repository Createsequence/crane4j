package cn.crane4j.extension.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author huangchengxing
 */
@Repository
public interface FooRepository extends JpaRepository<Foo, Integer> {
}
