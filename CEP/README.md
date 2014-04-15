Akka & Esper 연동테스트

[akka-processor <----> akka-esper]
akka-processor 에는 CEP 를 위한 이벤트 타입과 쿼리문이 정의되어 있다.
이를 akka-esper 로 보내서, 실제 CEP 과정은 akka-esper 에서 처리되도록 한다.
데이터 스트림에 대한 이벤트 처리 결과는 다시 akka-processor 에서 받을 수 있다.

[common]
akka-esper, akka-processor 사이의 remote 통신에 필요한
공통 클래스 및 오브젝트가 정의되어 있다. (event) 
jar 파일로 export 되어 akka-processor, akka-esper 프로젝트에 Library 로 추가돼 있다.

[테스트 방법]
1. run akka-esper
2. run akka-processor
3. In akka-processor, 채팅 메시지를 입력한다. (엔터로) 
4. 1초에 4개 이상 메시지가 입력될 경우 도배로 판단되어 메시지가 날아온다. 
