This document type is 'UNIX utf8'

java 1.6 이상에서 동작
윈도우의 경우는 run.cmd를 실행하면 됨
리눅스의 경우는 run.sh
환경파일은 
useServer.conf
svclist.conf

useServer.conf 는 기본운영환경을 설정합니다. 
이파일이 없어도 기본경로로 설정하여 진행합니다.

파일내용은 
svclist=svclist.conf ==> 점검용 환경파일이 들어있는 경로 기본값은 실행위치와 동일한 경로 
logpath=./logs ==> 결과 로그가 쌓이는 경로 기본값은 실행위치와 동일한 경로의 하위 logs경로로 설정됩니다.

단, 로그파일 경로(쓰기)와 svclist.conf(읽기) 에 대해  쓰기 권한과 읽기 권한이 없는 경우는 실행되지 않습니다.
유닉스 계열의 경우 로그디렉토리와 svclist.conf에 대해 접근 퍼미션을 반드시 확인하십시오


svclist.conf 은 점검대상의 서버목록을 기록한 설정파일로 진행중에 바꾸면 해당 동작을 하게됩니다. 
반드시 처음실행시 있어야 동작하며 서버 목록은 언제든지 수정 저장하면 1분 간격으로 읽어 들여서 동작합니다.
만일 점검목록에서 서버를 지우고자 할때는 해당 목록을 제거한 후 저장하면 자동 반영됩니다. 
파일의 형식은 
# * 0. 서비스 동작여부를 알려줌 1동작 0 멈춤 ==> 수정함 2017.06.15 일이후 부터는 2 이상은 서버에 접속시 연결을 대기하는 초단위 시간 주어진 시간이 지나도록 연결이 안되면 time out처리한다. 1인경우는 기본 30초
# * 1. 서비스 가능 종류 [http,https,tcp] tcp는 포트만 확인함.
# * 2. 서비스 아이피 또는 도메인 
# * 3. 서비스 포트 
# * 4. 서비스의 체크 간격 분단위
# * 5. 혹시 파일이나 가져올 것이 있는 경우 경로명  예제)/home/test/test.txt 또는 /test/test.html 없으면 공백
# * 6. 가져온 데이터에 체크 되어야할 값 예제)우리나라 대한민국 또는 serviceOk
# * 7. 접근시 로그인이 필요한경우 아이디{telnet,ftp,ssh} 추후 사용예정
# * 8. 접근시 로그인이 필요한경우 패스워드{telnet,ftp,ssh} 추후 사용예정
## 0/1:telnet[ssh|ftp|http|https|ssh|tcp]:ip:port:2:::::
#아래 설명
# [1]사용중,[https] 웹ssl,[www.google.co.kr] 도메인,[443] 포트 80과 443등 알려진 포트도 반드시 기입, [1] 1 분간격으로 점검, [/tour/index.html#/tour/mapservice?1] 이라는 url경로임, [Google Inc]라는 문구가 페이지 내에 있으면 정상  
1:https:www.google.co.kr:443:1:/tour/index.html#/tour/mapservice?1:Google Inc
:
로그 파일의 종류는 
access.log => 서버 점검상 이상이 없으면 점검할때마다 추가한다.
error.log ==> 서버 점검상 이상이 있으면 점검할때마다 추가한다. 
svclistsetup.log ==> 서버등록여부를 알려준다. 매분마다 갱신된다.
http/년월일시.log ==> http,https의 서비스에 대해서 헤더나 body의 문자열의 크기가 바뀌거나 실패성공이 바뀌거나 응답코드가 바뀌거나 1시간 간격으로 첫번째점검을 기록한다.

access.log 형식
년월이 시분초 도메인이름 포트 서비스종류 성공 http일때는 응답코드 메시지확인

error.log 형식
년월이 시분초 도메인이름 포트 서비스종류 실패 에러 메시지

svclistsetup.log 형식
NOT또는OK 다음은 svclist.conf의 리스트를 그대로 한줄씩 처리함.
OK 의 경우는 진행중이고 NOT은 인식 되지 않은 줄임.

http/년월일시.log 형식
년월일 시분초, url=url, web_status=done[false], web_respone=number, res_header_size=number, res_body_size=number, total_delay=ms number, delay= ms number, page_check=true[false]
web_status가 접속 done 성공 false 실패
web_respone http 응답 코드  responese Code표 참조
res_header_size http헤더 byte 길이
res_body_size 웹페이지 텍스트 byte 길이
total_delay 네트워크를 통해 열고 닫는데까지 걸린 micro time
delay 웹에 접속되어 body 데이터를 가져오는데 걸린 micro time
page_check 주어지 문자열이 해당 페이지에 있는지 확인해서 있으면 true 없으면 false


-------------- 2017.06.15 기능추가
1. 처음 시작할 때 0.2초 간격으로 사이트를 열기 시작하도록 함. 너무 빨리 열기 시작하면서 에러 발생하기 때문에 추가함
2. 환경의 첫번째 0또는1에서 1 이상도 등록 가능하도록 함.  2 이상은 서버에 접속시 연결을 대기하는 초단위 시간 주어진 시간이 지나도록 연결이 안되면 time out처리한다. 1인경우는 기본 30초
