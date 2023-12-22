package resource.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "CfgColumnSet", uniqueConstraints = {@UniqueConstraint(columnNames = "nameSet")})
public class CfgColumnSet {

	@Id	
	@Column(name = "nameSet", unique=true, nullable = false, length = 256) public String nameSet;
	
	@Column(name = "columns", columnDefinition = "TEXT") public String columns;
	@Column(name = "columnsDisplay", columnDefinition = "TEXT") public String columnsDisplay;
	@Column(name = "description", columnDefinition = "TEXT") public String description;

	
}
