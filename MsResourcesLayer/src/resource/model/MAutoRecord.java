package resource.model;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.UpdateTimestamp;


//https://howtodoinjava.com/hibernate/hibernate-hello-world-application/

@Entity
@Table(name = "MAutoRecord", uniqueConstraints = {@UniqueConstraint(columnNames = "ID")})
public class MAutoRecord implements Serializable{

	
	/**
	 * 
	 */
	private static final Long serialVersionUID = -645122410143974356L;


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", unique=true, nullable = false)
	public Integer recordID;
	
	
	//time capture
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_date")
	public Date createDate;

	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modify_date")
	public Date modifyDate;
	
	@Column(name = "hostname", nullable = false, length = 64)
	public String hostname;
	
	/**
	 * time reach server roundtrip
	 */
	@Column(name = "rtt_time_RoundTrip_Ms") public Double rtt_time_RoundTrip_Ms = 0.0;
	@Column(name = "rtt_latency_Ms") public Double rtt_latency_Ms = 0.0; 
	@Column(name = "rtt_speed_KBps") public Double rtt_speed_KBps = 0.0;
	@Column(name = "rtt_drops_Percent") public Double rtt_drops_Percent = 0.0;
	@Column(name = "rtt_speed_upload_KBps") public Double rtt_speed_upload_KBps = 0.0;
	@Column(name = "rtt_speed_download_KBps") public Double rtt_speed_download_KBps = 0.0;
	@Column(name = "rtt_content_chars") public Double rtt_content_chars = 0.0;
	@Column(name = "client_ext_ip", length = 256) public String client_ext_ip = "";
	
	
	//network
	@Column(name = "speed_upload_Bps") public Float speed_upload_Bps= 0.0f;
	@Column(name = "size_upload_Bytes") public Float size_upload_Bytes= 0.0f;
	@Column(name = "speed_download_Bps") public Float speed_download_Bps= 0.0f;
	@Column(name = "size_download_Bytes") public Float size_download_Bytes= 0.0f;
	@Column(name = "time_namelookup_ms") public Float time_namelookup_ms=   0.0f;
	@Column(name = "time_connect_ms") public Float time_connect_ms=  0.0f;
	@Column(name = "time_appconnect_ms") public Float time_appconnect_ms= 0.0f;
	@Column(name = "time_pretransfer_ms") public Float time_pretransfer_ms=  0.0f;
	@Column(name = "time_redirect_ms") public Float time_redirect_ms=  0.0f;
	@Column(name = "time_starttransfer_ms") public Float time_starttransfer_ms= 0.0f;
	@Column(name = "time_total_ms") public Float time_total_ms=  0.0f;
	
	//host
	@Column(name = "arch", length = 128) public String arch;
	@Column(name = "cpu_cores") public Integer cpu_cores;
	@Column(name = "cpu_usage") public Double cpu_usage;
	@Column(name = "ram_total_MB") public Long ram_total_MB;
	@Column(name = "ram_usage") public Double ram_usage;
	@Column(name = "client_ip", length = 2048) public String IP;
	 //soa auto
	@Column(name = "soa_webServer", length = 128) public String soa_webServer;
	@Column(name = "soa_login_time_ms") public Long soa_login_time_ms;
	@Column(name = "soa_search_time_ms") public Long soa_search_time_ms;
	@Column(name = "soa_search_result_cnt") public Long soa_search_result_cnt;
	@Column(name = "soa_expand_structure_time_ms") public Long soa_expand_structure_time_ms;
	@Column(name = "soa_expand_structure_lines") public Long soa_expand_structure_lines;
	@Column(name = "soa_logout_time_ms") public Long soa_logout_time_ms;
	@Column(name = "soa_total_time_ms") public Long soa_total_time_ms;
	@Column(name = "soa_poolHostName", length = 128) public String soa_poolHostName;
	@Column(name = "soa_tcServerID", length = 128) public String soa_tcServerID;
	@Column(name = "soa_syslogFile", length = 256) public String soa_syslogFile;
	@Column(name = "soa_idUserTest", length = 64) public String soa_userTest;
	@Column(name = "soa_groupTest", length = 256) public String soa_groupTest;
	@Column(name = "soa_testDataContext", columnDefinition = "TEXT") public String soa_testDataContext;
	@Column(name = "msr_measureFolder", length = 256) public String msr_measureFolder;
	@Column(name = "msr_filesLink", columnDefinition = "TEXT") public String msr_filesLink;
	
	//current pool running tcserver
	@Column(name = "cpool_cpu_cores") public Integer cpool_cpu_cores;
	@Column(name = "cpool_cpu_usage") public Double cpool_cpu_usage;
	@Column(name = "cpool_ram_total_MB") public Long cpool_ram_total_MB;
	@Column(name = "cpool_ram_usage") public Double cpool_ram_usage;
	
	@Column(name = "cpool_NUM_ASSIGNED") public Integer cpool_NUM_ASSIGNED;
	@Column(name = "cpool_USE_PERC") public Double cpool_USE_PERC;
	
	//all pools, overral
	@Column(name = "apool_NUM_ASSIGNED") public Integer apool_NUM_ASSIGNED;
	@Column(name = "apool_USE_PERC") public Double apool_USE_PERC; //avg
	
	//io, db
	@Column(name = "db_tbl_escalate_index") public Integer db_tbl_escalate_index;
	@Column(name = "diskio_bwMbSec") public Integer diskio_bwMbSec;

}
