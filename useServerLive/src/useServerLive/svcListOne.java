package useServerLive;
/**
 * 서버의 기본정보를 담는 객체선언입니다.
 * @author allco
 *# * 0. 서비스 동작여부를 알려줌 1동작 0 멈춤
# * 1. 서비스 가능 종류 [http,https,telnet,ftp,ssh,tcp] tcp는 포트만 확인함.
# * 2. 서비스 아이피 또는 도메인 
# * 3. 서비스 포트 
# * 4. 서비스의 체크 간격 분단위
# * 5. 혹시 파일이나 가져올 것이 있는 경우 경로명  예제)/home/test/test.txt 또는 /test/test.html 없으면 공백
# * 6. 가져온 데이터에 체크 되어야할 값 예제)우리나라 대한민국 또는 serviceOk
# * 7. 접근시 로그인이 필요한경우 아이디{telnet,ftp,ssh}
# * 8. 접근시 로그인이 필요한경우 패스워드{telnet,ftp,ssh}
## 0/1:telnet[ssh|ftp|http|https|ssh|tcp]:ip:port:2:::::
 */
public class svcListOne {
	private int svcRun;//1점검 0은 점검하지 않음
	private String svcType;//telnet[ssh|ftp|http|https|ssh|tcp]
	private String svcIp;//아이피 또는 도메인
	private int svcPort;//서비스 포트
	private int chkTime;//서비스 체크 간격 분단위
	private String chkPath;//서비스 체크할 경로
	private String chkMsg;//체크할 문자열
	private String logId;
	private String logPs;
	private int timeOut=30;//타임아웃 대기 초
	//로그기록에 사용하는 것들
	private String conectStr;//연결 완료 여부 done, false
	private int responseCode;//응답 코드
	private long fileSize;//파일사이즈
	private int headerSize;//헤더사이즈
	private long pageTime;//페이지 응답시간
	private long workTime;//총작업시간
	private String strEncode="";//문자코드
	private boolean pageChek;//페이지 체크 결과값
	
	//기록을 관리하는 과정에 변경될때만을 위한 설정값
	private boolean changeLog=true;//데이터 값이 변경됨을 감지하여 처리한다. 만일 저장을 하면  초기화 한다.
	private String oldlogfile="";//변경체크로 관리할때 사용하는 것으로 로그파일명이 들어간다.
	/**
	 * 로그의 초기화
	 */
	public void defaultLog(){
		this.conectStr = "false";
		this.responseCode = 0;
		this.fileSize = 0;
		headerSize=0;
		this.pageTime = 0;
		this.workTime = 0;
		this.strEncode="";
		this.pageChek=false;
		this.timeOut = 30;
	}
	/**
	 * 환경을 변경
	 * @param o
	 */
	public void setSvcListOneConf(svcListOne o){
		setSvcRun(o.getSvcRun());
		setSvcType(o.getSvcType());
		setSvcIp(o.getSvcIp());
		setSvcPort(o.getSvcPort());
		setChkTime(o.getChkTime());
		setChkPath(o.getChkPath());
		setChkMsg(o.getChkMsg());
		setLogId(o.getLogId());
		setLogPs(o.getLogPs());
	}
	/**
	 * 로그의 변경
	 * @param o
	 */
	public void setSvcListOneLog(svcListOne o){
		boolean runOk = false;
		if(!conectStr.equals(o.getConectStr())){conectStr =o.getConectStr();runOk=true;}
		if(responseCode!=o.getResponseCode()){responseCode =o.getResponseCode();runOk=true;}
		if(fileSize !=o.getFileSize()){fileSize =o.getFileSize();runOk=true;}
		if(headerSize !=o.getHeaderSize()){headerSize =o.getHeaderSize();runOk=true;}
		pageTime =o.getPageTime();
		workTime =o.getWorkTime();
		strEncode =o.getStrEncode();
		pageChek = o.isPageChek();
		if(!o.isPageChek()){runOk=true;}// 실폐하면 무조건 로그 기록해야한다.
		if(runOk) changeLog = true;//수정 되었음을 알린다.
	}

	public int getSvcRun() {
		return svcRun;
	}
	public void setSvcRun(int svcRun) {
		this.svcRun = svcRun;
	}
	public String getSvcType() {
		return svcType;
	}
	public void setSvcType(String svcType) {
		this.svcType = svcType;
	}
	public String getSvcIp() {
		return svcIp;
	}
	public void setSvcIp(String svcIp) {
		this.svcIp = svcIp;
	}
	public int getSvcPort() {
		return svcPort;
	}
	public void setSvcPort(int svcPort) {
		this.svcPort = svcPort;
	}
	public int getChkTime() {
		return chkTime;
	}
	public void setChkTime(int chkTime) {
		this.chkTime = chkTime;
	}
	public String getChkPath() {
		return chkPath;
	}
	public void setChkPath(String chkPath) {
		this.chkPath = chkPath;
	}
	public String getChkMsg() {
		return chkMsg;
	}
	public void setChkMsg(String chkMsg) {
		this.chkMsg = chkMsg;
	}
	public String getLogId() {
		return logId;
	}
	public void setLogId(String logId) {
		this.logId = logId;
	}
	public String getLogPs() {
		return logPs;
	}
	public void setLogPs(String logPs) {
		this.logPs = logPs;
	}
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public long getPageTime() {
		return pageTime;
	}
	public void setPageTime(long pageTime) {
		this.pageTime = pageTime;
	}
	public long getWorkTime() {
		return workTime;
	}
	public void setWorkTime(long workTime) {
		this.workTime = workTime;
	}
	public String getStrEncode() {
		return strEncode;
	}
	public void setStrEncode(String strEncode) {
		this.strEncode = strEncode;
	}
	public boolean isPageChek() {
		return pageChek;
	}
	public void setPageChek(boolean pageChek) {
		this.pageChek = pageChek;
	}

	public int getHeaderSize() {
		return headerSize;
	}

	public void setHeaderSize(int headerSize) {
		this.headerSize = headerSize;
	}

	public String getConectStr() {
		return conectStr;
	}

	public void setConectStr(String conectStr) {
		this.conectStr = conectStr;
	}

	public boolean isChangeLog() {
		return changeLog;
	}

	public void setChangeLog(boolean changeLog) {
		this.changeLog = changeLog;
	}
	public String getOldlogfile() {
		return oldlogfile;
	}
	public void setOldlogfile(String oldlogfile) {
		this.oldlogfile = oldlogfile;
	}
	public int getTimeOut() {
		return timeOut;
	}
	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}	
}
