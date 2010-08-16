package org.sigmah.server.domain.value;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.sigmah.server.domain.User;

/**
 * 
 * @author Denis Colliot (dcolliot@ideia.fr)
 */
@Entity
@Table(name = "file_version", uniqueConstraints = { @UniqueConstraint(columnNames = {
																						"id_file",
																						"version_number" }) })
public class FileVersion implements Serializable {

	private static final long serialVersionUID = -1143785858180618602L;

	private Long id;
	private File parentFile;
	private Integer versionNumber;
	private String path;
	private User author;
	private Date addedDate;
	private Long size;

	public void setId(Long id) {
		this.id = id;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id_file_version")
	public Long getId() {
		return id;
	}

	public void setParentFile(File parentFile) {
		this.parentFile = parentFile;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_file", nullable = false)
	public File getParentFile() {
		return parentFile;
	}

	public void setVersionNumber(Integer versionNumber) {
		this.versionNumber = versionNumber;
	}

	@Column(name = "version_number", nullable = false)
	public Integer getVersionNumber() {
		return versionNumber;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Column(name = "path", nullable = false, length = 4096)
	public String getPath() {
		return path;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_author", nullable = false)
	public User getAuthor() {
		return author;
	}

	public void setAddedDate(Date addedDate) {
		this.addedDate = addedDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "added_date", nullable = false)
	public Date getAddedDate() {
		return addedDate;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	@Column(name = "size", nullable = false)
	public Long getSize() {
		return size;
	}
}