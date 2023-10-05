

# project algo

보드게임 다빈치코드(Algo) 를 간단히 구현하기 위한 프로젝트

클라이언트 & 서버 개발 : GoF9490

프론트 기술스택 : Unity 3D, WebSocket Sharp \
백 기술스택 : java 11, spring 2.7.10, spring data jpa, spring websocket

# 시연 영상
[![Video Label](https://img.youtube.com/vi/ut38YT0HRRI/0.jpg)](https://youtu.be/ut38YT0HRRI)

# 클라이언트 다운로드 링크

PC : https://drive.google.com/file/d/1BUQDARsqtjmtEBa0YSYTpcMgbOrhl05c/view?usp=sharing

Android APK : https://drive.google.com/file/d/1gcZIOrDUJdty4rkmmF6rnF6wD69omHLn/view?usp=sharing

# 사용방법

    1. 클라이언트를 플랫폼에 알맞게 다운로드 후 실행합니다.

    2. 게임이 정상적으로 실행되면 터치를 해서 웹서버와 웹소켓을 연결합니다.

    3. 연결이 정상적으로 완료되었으면 게임에서 일회용으로 사용할 닉네임을 작성하고 로그인합니다.

    4. 이후 게임 방을 만들지 찾을지 선택해서 참여 후 게임을 진행하시면 됩니다.

# 진행방식

    [Phase : WAIT]
        * 플레이어들이 방에 입장합니다. (최소 2명, 최대 4명)
        * 입장한 모든 플레이어가 준비를 완료하면 게임이 시작됩니다.

    [Phase : SETTING]
        * 0~11의 숫자가 적힌 블록을 흰색 검은색 각각 배치합니다.
        * 게임 시작 후 순서가 랜덤으로 정해집니다.

    [Phase : START]
        * 정해진 순서대로 2~3인 게임은 4개, 4인 게임은 3개의 블록을 색깔에 관계없이 가져옵니다.
        * 패는 오름차순 순서대로, 숫자가 같으면 검은색이 왼쪽에 오도록 정렬합니다.
        * 각자 패가 세팅이 되면 '-' 형태를 가진 조커블럭을 흰색 검은색 블럭 각각에 배치합니다.
        * 이후 플레이어는 순서대로 아래의 과정을 진행하게 됩니다.

    [Phase : DRAW]
        * 흑과 백 블록중에 어떤색을 뽑을것인지 선택하고, 해당 색의 남아있는 블록중 랜덤으로 하나를 뽑습니다.

    [Phase : SORT]
        * 뽑은 블록을 알맞은 위치에 자동으로 정렬합니다.

    [Phase : GUESS]
        * 뽑았던 블록을 티가 나게 배치하고 상대방의 블록중 하나를 선택 후 그 블록의 숫자를 추리합니다.
        * 맞출시 REPEAT 페이즈로, 틀릴 시 뽑았던 카드를 오픈하고 END 페이즈로 넘어갑니다.
        * 만약 이번 추리로 자신 이외에 모든 플레이어가 아웃되었다면 GAMEOVER 페이즈로 넘어갑니다.

    [Phase : REPEAT]
        * GUESS 페이즈를 다시 할지, 뽑았던 카드를 집어넣고 END 페이즈로 넘어갈지 선택하여 해당 페이즈를 진행합니다.

    [Phase : END]
        * 해당 플레이어의 턴을 종료하고 다음 플레이어에게 순서를 넘깁니다.
        * 다음 플레이어는 DRAW 페이즈부터 시작하여, 게임이 끝나기까지 앞의 과정을 반복합니다.

    [Phase : GAMEOVER]
        * 마지막까지 살아남은 플레이어가 승리하게 됩니다.
        * 게임이 끝나면 WAIT 페이즈로 넘어가고 이후 게임을 다시 하거나 방을 나가거나 할 수 있습니다.


# 프로젝트 리뷰

## 기술스택
    
    게임 서버로는 spring 보다 Node.js 가 더 많이쓰인다는것을 알고는 있었지만, 
    spring 프레임워크를 배우는 입장에서 현재 배우고있는 기술로 게임서버를 만들어보고 싶었습니다.

    클라이언트로는 과거에 조금 배운적 있는 Node.js 나 React 도 생각해보았으나, 그보다 상대적으로 경험이 많은 Unity3D 엔진을 사용하여 
    제 경험을 활용함과 동시에 'Unity3D 클라이언트 + spring 게임서버' 라는 나름 유니크한 기술스택에 도전해보고 싶었습니다.

    게임서버의 특성상 클라이언트가 요청하지 않아도 다른 플레이어에 의해 데이터가 갱신되야하는 경우가 많기에,
    WebSocket 방식과 SSE 방식 사이에서 고민을 했었습니다.
    이 부분에서 제가 사용한 Unity3D 엔진은 C# 언어 기반이고, C# 언어에 대응하는 SSE 라이브러리를 마땅히 찾지 못하여
    C# 기반의 WebSocket 라이브러리인 WebSocket Sharp를 채용해 WebSocket 방식으로 정하였습니다.
    (추후에 SSE 방식을 추가하거나 변경할 가능성도 있습니다.)

    DB는 테스트코드를 동일한 환경에서 돌리게끔 Embedded 모드를 지원하고, SQL DB와 호환성이 좋으며, 
    가볍게 사용 가능한 H2 DB를 채택하였습니다.

    플레이어가 누구나 접근하기 쉽도록 가볍게 만들 생각이며, 개인정보 또한 필요없고 플레이어의 중요한 정보를 보관하지 않을 생각이기에,
    Spring Security가 없어도 될거같다는 생각이 들어 채용하지 않았습니다.

## ERD

![algo_erd](https://github.com/GoF9490/project-algo/assets/81549749/76890b1f-4774-4808-a037-72bb4cae6912)

    GameRoom 과 Player 엔티티를 양방향 연관관계로 설정하였습니다.

    Block 객체를 엔티티로 DB에 넣어 저장할경우 이후 블록들을 다시 가져올 때,
    저장했을 당시의 순서를 보장받지 못하고 DB에 저장된 순으로 가져오는 문제점을 겪었습니다.
    해당 부분에서 게임의 재미의 키포인트 중 하나인 조커 블록의 순서를 어떻게 유지할지 고민을 했습니다.

    Block 데이터의 순서를 보장받기 위해 @Converter 를 통해 blockList를 알맞은 규칙에 맞게 String으로 저장, 
    이후 DB에서 불러올 때 객체로 변환해서 가져오게끔 설계했습니다.

## 코드 & 로직 리뷰

### WebSocketHandler

    MessageDataRequest 클래스를 만들어 요청에대한 형식을 정의했습니다.
    MessageDataRequest는 MessageType 라는 enum형식의 type 이란 변수와 String형식의 message 변수로 이루어져있습니다.
    type 변수는 핸들러에서 switch문으로 MessageType 값에 따라 알맞는 메서드를 실행하도록 알려주는 역할을 합니다.
    (Spring MVC 패턴에서 Handler Mapping 과 Handler Adapter 역할을 대신합니다.)

    MessageType의 값들은 Request DTO & Response DTO 객채의 이름으로 구성되어있으며, 
    클라이언트에서도 같은 이름과 필드값의 객체로 맞춰서 사용합니다.
    
    클라이언트에서 Request DTO 를 보내면 그 타입에 맞게 WebSocketHandler에서 핸들링을 하고(Handler Mapping 의 역할),
    String 형색의 message를 ObjectMapper를 통해 해당 객체로 알맞게 변환하고(RequestBodt의 역할),
    이후 Controller의 알맞은 메서드에 변환한 DTO를 인자로 전달해 실행합니다(Handler Adapter의 역할).

### 블록의 정렬

    해당 게임은 0 ~ 11 까지의 숫자와 조커(-)가 적힌 흰색과 검은색 블록이 존재합니다.
    숫자 블록만 있으면 정렬이 편하지만, 조커블록의 존재로 인해 정렬방식에 애를 먹었습니다.
    원래는 카드를 추가할때마다 블록 전체에 대한 정렬 메소드를 실행하였으나, 
    조커블록의 위치를 유지하기 까다로워 블록을 추가할 때만 올바른 위치를 찾아 추가하고 해당 배열을 유지하게끔 바꾸었습니다.
    
    Block 객체에 boolean 값을 리턴하는 comparePosition 라는 메서드를 따로 만들어
    자신과 해당 자리의 블록의 숫자를 비교하고 클 경우 true, 작을경우 false를 반환하게끔 작성,
    true가 나올때까지(즉 ,자신보다 큰 자리의 수가 나올때 까지) BlockList를 탐색하고,
    나올경우 해당 블록의 index값을 받아와 그 위치로 뽑은 블록을 add 합니다.
    모두 탐색하여 false만 나올경우 가장 마지막 index로 블록이 추가됩니다.
    
    숫자가 같을경우 BlockColor에 있는 code값으로 다시 비교해 검은색이 더 작은수로 인식되며, 
    조커의 경우 자신이든 상대든 false를 리턴하게끔 하여 탐색에 지장이 없게끔 하였습니다.

    조커 블록은 updateJokerIndex 메서드에 따라 위치가 변할 수 있으며,
    클라이언트 조작 등으로 인해 본래 게임 규칙과 맞지않은 위치에 읨의로 업데이트하는것을 방지하기 위해,
    jokerRange 변수를 통해 부정행위를 방지하도록 했습니다.

### @Converter

    ERD에서 언급이 되었던 문제입니다.
    '블록의 정렬' 파트에서 기존의 매 동작마다 sort 메서드를 실행하던 방식을 고치니 DB에서 blockList 값을 가져올 때,
    저장했을 당시의 순서를 보장받지 못하였습니다. (당시만 하더라도 Block 객체를 @Embeddable 형식으로 사용하고 있었습니다.)
    
    다행히 Block 객체는 가지고있는 필드값이 적고 복잡하지 않았기에 문자열로 표현이 가능해보였고,
    BlockArrayConverter를 만들어 특정 규칙에 맞게 문자열로 저장, 가져올땐 객체로 변환되서 사용할 수 있도록 설계했습니다.

    규칙은 흰색 블록은 앙수, 검은색 블록은 음수. 
    이후 알맞은 숫자(조커는 12)가 적히고 'o'와 'c' 단어로 open과 close 상대를 표현합니다.

# 아쉬운 점

    맨 처음 설계는 클라이언트가 앱 형식이 아닌 웹에서 작동하도록 구상하고 접근했었습니다.
    (과거 'Surviv.io' 나, '작혼' 같은 멀티플레이 형식의 가볍게 접근가능한 웹게임 느낌을 주고싶었습니다.)
    이 과정에서 Unity3D엔진을 채택한 이유도 Unity3D엔진에서 WebGL로 포팅을 해주는 기능이 있었기 때문입니다.

    해당 기술을 검토하면서 WebGL로 포팅 이후 AWS S3에 올리면 정상적으로 실행이 되는것도 확인을 했으나,
    Unity3D 엔진이 WebGL 에서는 웹소켓을 지원하지 않는다는 것을 뒤늦게 파악하여,
    일단은 웹소켓을 지원하는 앱 형태로 노선을 바꾸어 완성하게 되었습니다.

    이 부분에서는 충분한 기술검토 후 웹소켓이 아닌 방식(SSE 유력)을 사용해 WebGL로 포팅하거나,
    웹소켓 방식을 유지하면서 클라이언트를 교체(Node.js, React 등등)할 의향이 있습니다.

# 추가 작업 (예정)
    
    * GameService 클래스 책임 분리.

    * WebSocket을 사용하지 않고 SSE 방식으로 가능하다면 기능을 추가하거나 변경하기.
    (Unity3D 클라이언트 쪽도 포함해 전체적으로 기술검토후 가능해보인다면 접근할 것.)

    * GameWebSocketMessageController 클래스 책임 분리.
    (위의 과정을 거치면 자연스럽게 이루어지거나 필요성이 없어질 수도 있음.)
