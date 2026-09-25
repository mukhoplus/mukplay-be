package com.mukplay.config;

import com.mukplay.common.entity.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DataSourceTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TestSampleRepository testSampleRepository;

    @Test
    @DisplayName("DataSource 빈이 주입되고 커넥션을 획득할 수 있어야 한다")
    void testDataSourceConnection() throws Exception {
        assertThat(dataSource).isNotNull();
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.isValid(1)).isTrue();
        }
    }

    @Test
    @DisplayName("JPA 엔티티 저장 및 BaseTimeEntity Auditing이 정상 동작해야 한다")
    void testJpaEntityAuditing() {
        TestSample sample = new TestSample("sample-name");
        TestSample saved = testSampleRepository.save(sample);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("sample-name");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
}

@Entity
@Table(name = "test_samples")
@Getter
@NoArgsConstructor
class TestSample extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public TestSample(String name) {
        this.name = name;
    }
}

@Repository
interface TestSampleRepository extends JpaRepository<TestSample, Long> {
}
