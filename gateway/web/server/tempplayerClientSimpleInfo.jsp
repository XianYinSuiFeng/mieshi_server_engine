<%@ page contentType="text/html;charset=utf-8" import="java.util.*,
com.xuanzhi.tools.text.*,java.lang.reflect.*,com.xuanzhi.tools.queue.DefaultQueue,
com.xuanzhi.tools.transport.*,java.nio.channels.*,com.fy.gamegateway.mieshi.server.*"%>
<%

		
StatManager sm = StatManager.getInstance();
SimpleEntityManager<ClientAccount> em = sm.em4Account;
String[] usernames = new String[]{"skyfxzd3","skyfxzd4","skyfxzd5","skylove","skyqblt01","skyqblt02","skyqblt03","skyqblt04","skyqblt05","skywlsd20","skyyl001","skyyl002","skyyl003","skyyl004","skyyl006","skyyl007","skyyl008","skyyl009","slkwsh","sllalm5271","slq0213","smallyangbo","smilells","smilev64050","smith5770","smith57700","smoekgui","smsfei","snakezhukai","snauau","snowkiss2002","snowkiss2008","snowly","snowy520","snowyoyo","snowzzz","snqjh000","snsdsj520","snwangfeng","snyt163","soiao1121","solaforever","soloby13","song125464","song6613130","song79125031","song793","songguangtao","songming21","songmingd21","songwen213","songzhichao","SOOELL","soon01","sorkings","sos315344699","soslover","soso5240","souaix3","soul123","spottydan","sprite6724","spriteangel0","sq516236965","sqawyx","sqitzm","sqlong","squaw111","squaw520","sr1019","sr19851019","ss596812373","ss709859637","ss74521ss","ss7755818","ssddsss11","ssh4033522","sshenxin2011","ssjjll","sskfn1987","ssongjiann","sspsep101","sspsep102","sssss56","sssssss7","stanley8185","starak","stellviavip","stephentsai","stephy1212","steve982001","stick999","stklv001","stm20008","stx520","stx850923","stxs0754","su5884624","sua12345","sua123456","suansuanyiwei","sugar1022","sugerting","sugus1007","suhaojie","suimeer","suiyuewuhen","sujia0817","sujun58","suki80592499","sukisuki","sumboy","sumiao1986","summer0301","sun0921","sun1445200","sun19749276","sun198188","sun1991zhen","sun222222","sun2269368","sun2827","sun3755","sun405525498","sun521xy","sun5953465","sun654887","sun6571753","sun84110","sun844665","sun8854681","sun970317","sunbaozxc","Sunbiao","sunbin0301","sunbin0424","sunbin1120","sunbinbin","sunbinbinbin","sunbing250","suncc220","sunfang011","SUNHAOGG","sunhoujing","sunhu080718","suning111","suning222","sunjixi0618","sunkun2010","sunmenghao1","sunming123","sunny4444","sunnyfirst","sunnytou","sunqing52111","sunqizhe521","sunshi1986","sunshuai521","sunsky","sunsky1239","sunss111","sunss222","sunwenqi1993","sunwusuobai","sunwuu","sunxiaoxu521","suny0616","sunyulong","sunzhen","sunzhiqiang","suojing87","superfu","surpers12","sus1171","susuzuku","suwy123","suxiaoao123","suyu123","suzuki620","sven080518","sw19890410","sweetycaicai","swl566","swolkran","sword2","sx2mao","sx3457","sxddsl","sxfggs","sxh07612","sxl4726407","sxlaini","sxs19890906","sxtiger","sxycwza","sxyuanoi","sy1995","sy317005306","sy520an","sy88628lh","syaj123","syboy130","syc10221105","syg260188","syh19930325","syj1229","syjry888","sykusa","sylovelza","symantec503","syncmaster78","syswlp125","syt125584719","sywerpeng","sywerweb","syx189009972","syztcwzw","syzxtgc","sz99617977","szaf882279","sze1234","szericqiu","szfei10","szj309189986","szlyyxwd","szz_wd","t07872228","t11262324","t13797728178","t15896498731","t1987126","t232115581","t570471664","t770978000","tagx789","taile0","talonweimila","tan456","Tan520","tan9896850","tandoudou123","TANG11751","tang871025","tang900604","tangjiajun","tangjun566","tangtang530","tangtao123","tangtingfl","tanguoping","tangweicong","tangwenkai","tangying85","tanjunjie","tank2407255","tanxiao111","tanxiaofeng","tanxiaofeng8","tanxinxiong3","tanxufeng1","tanzupeng112","tao1234567","taodahao777","taodahao789","taodahao999","taoguoliang","taolele","taotie","taowei123JJ","taoyong8895","taoyong8897","taozecheng","taozi212","tb260197","tc1991","tch88113761","tdc647","tdl664591203","teddyzz","TEng0319","teng8173566","tengxiang","tengxing","tengxing2012","tengxun","Terence","terry5360","terryhaido","tert03878","tessy0116","tf1013823512","tf213626","tf702030","tf830314","tfllyftm","tg1201429","tg201027308","tglook200910","tgtiancai","tgw1256","theer1nnyes","thj234463616","tian1202","tian521","tian521119","tian7228349","tianmeng","tianputian","tianshi123","tiansisid","tianwei1999","tianxiajinji","tianyipantia","tianyu106","tianzhisong","tianzui444","tiao43848640","tiaowuba12","tidusnwx","tidy026","tigger","timki8876","timqhk","tina0521","ting7928","ting97411","tingda","tingting5201","tintin3741","tj17956","tj2330","tj84817956","tj84882357","tj93929703","tjc1990","tjjtjjtjj123","tk93560","tkuser","tmjhlj","tmjiuxi","tmz123","toloveu715","tom13144","tom185622","tommychiu","tommylsm","tomng789","tompson4","tong1020","tong1333","tong931106","tong9394","tonyan198702","tonymami","tonywot","tonyzhu","topkeke","topkeke17770","topkeke17777","topymlove","touhuan8023","tpb3724","tpsd007","tpt123","tqtiiaw","troubleliu","TRY123456","tslong112234","tsuntsun","tswl88","tsxg713","tt498031899","tt5155","tt97429052","ttdayi","ttshuaya007","ttsky123","ttwb367","ttwb369","ttxs2008","ttxs2012","ttyueyue3344","tubolin","tuboshu330","tuchudewo","tudou815","tujiqing4","tuo161604","turuishaxing","tushiming87","tutou588","tutu178888","tuxianqiu","tuzi1986","TVE2000","twan608","twmjz82","twtamdwfn112","tx290398326","txd0816","txh186611614","txh19920916","txyxren","txz846711","txz886","txzhfeng4","tycoon988","tyj807","tyj821020","tyk19930320","tyy1223","tyy1314","tz110415","TZBDLiebao","tzp19911129","tzwormxx","tzyhlzp","u9500fxp","uc80093321","uctxg83","udbqqq","uff158","ufoft2","ufooo3","uk1861","ukawa416","uncobaby","unsucked","Uqikir","ussr509","uu2511","uuskylove","vampire112","vannio01","vannio02","vanxcc","vap13533577","vc12346","vcvc509","velina","veng21617","veron8933","vgghhfrt","vickiop","vicky007sun","vicsky1","victoria","vikoxue","vilantom1","vilantom2","village0903","vince_qiu","vincent2005","vincentchen","vincentzh","vincentzh5","vinegar","vio10010","violin.zp","vip1080012","vip2113110","vip2113111","vip2113112","vip2113113","vip2113116","vip2113117","vip2113118","vip2113119","vip3166","vipnb878","vipp211311","vipxkw","visensun","vito20107","vivahua1","vivick","vkedo21","VoveZE","vpxiao","vr2006dwj","vs125521","vvanlw","vvhim1523","w0716w","w111352","w118115","w1281213992","w13032545685","W13057664","w13069959674","w1312111","w132168","w1363578","w1406864112","w1472580","w149739286","w15276369177","w153691863","w1834587890","w18677558059","w19770202","w19870319","w19890714h","w2440097768","w2513048685","w2629285","w2868611","w33473347","w4224574","w437399658","w450419378","w451087342","W4512023","w460074127","w470085895","w496530234","w5150573","w515322","w521122","w525628174","w5281627","w5443824","w5533048","w553740086","W5647165","w564926775","w583711622","w621112","w6223391","w6223556","w627201898","w63490022","w6474151","w6478719","w6895522","w740476309","w7480549","w751285884","w761200387","w772037645","w8208247","w86688883","w870515","w8846256","w921111790","w9264546","w926454l","w975471319","W999999999","wa492153318","wa962464","waige820305","waiperjoy","waisongg1","wally347","wan123789","wan1237890","wan152016078","wan22749073","wan5188","wan77646","wan881225","wan974674824","wanduck","wane45","wane46","wang0369241","wang0525","wang0951","wang100904","wang102399","wang1114594","wang1128AH","Wang123","wang123412","wang1234aa","wang152","wang18447933","wang250156","wang305872","wang3736","wang3908693","wang510320","wang520chao","wang560324","wang66997070","wang69408584","wang73546748","wang7365","wang885200","wang9009","wang9527","wang983626","wang9999","wanganwei1","wanganwei3","wangbelcanto","wangbin83090","wangbo002","wangche","wangdengxin","wangdezhu","wangfengyu","wangfuqiang","wangfyxf119","wanghaoda","wanghou","wangjian520","wangjicheng","wangjie911","wangjihua092","wangjunming","wangke1366","wangkerang","wanglei111","wanglei123","wanglei12332","wangleihppy","wangli4247","wanglingya","wanglinlin90","wangluocuowu","wangmq","wangnaixin","wangnamo","wangnan77","wangpengnj","wangping2tys","wangqi123456","wangqi1990","wangqi262021","wangqi369","wangranran","wangrui6688","wangse90","wangsenxf","wangshen1","wangsheng518","wangshuo","wangtian","wangtianlei","WangTtao","wangtu","wangwang4304","wangwei12","wangxian2","wangxian90","wangxiandl","wangxiang92","wangxianhai","wangxiao9023","wangxinmin","wangxu1985","wangya66","wangyan524","wangyibai123","wangyifei","wangyijun","wangyiyi","wangyuhang","wangyuyang","wangzegui","wangzhe789","wangzhi87061","wangzhigang","wangzhiyuan7","wangzixuan12","wanhongyou","wanjia12311","wanjia123111","wanjin119","wanjunqing","wanleiming99","wanlhj2012","wannawm","wanoyang","wanren","wansong","wanwan521","wanwode88","wanwu115","wanwu139319","wanyouxi94","wanyxj","wap1860","warsept08","wawa111","wawa54418","wayne0919","wayne1977","waz002136","wazn3235558","wazyzc","wb121801","wb442762042","wb76503","wblhjx","wby900605","wcb265","wcb616","wcl880616","wcq21588","wcq215880","wcy24391","wcylion","wczqq0127","wd345714327","wd8638899","wdl5705568","wdn110","wdn113","wdn123","wdn220","wdw031323","wdw1312","wdybjzd","wdz172270076","wdza521","wdzaaa","we1984","we1984we001","we1chunyu","wei2006814","wei2090","wei325803","wei4021","wei416948489","wei4211","wei4563440","wei582823788","wei6363982","wei787858","wei8807","wei890420","weiaizhu0427","weichao0204","weichao0705","weicong","weigang114","weigang39","weihong717","weihongbo137","weihongyu","weihouyong","weike921208","weilai1001","weilanshab","weilesm123","weiliang1987","weilu2012","weiqian520","weir518","weishaobo","weishenme","weishusen","weiwei101","weiwei130","weiwei1979","weiwei520131","weiwei693436","weixiao","weixiaodong","weiyi5257","weiyongle","weiyuhuajian","weizhidan","weizhidan001","weizi717","wellkill00","wen0820","wen1788188","wen1983","wen1984","wenbin1","wenchangjie","wencolin1101","wendao","weng0430","weng623","wengjian123","wengjian198","wengjian998","wengyuewen90","wenhaining","wenjing6275","wenjunli1972","wenjunwan","wenkaidi","wenmin","wenqiwei","wenshu1987","wenxuan521","wenyan1310","wenyi33330","wenyi3333330","wenzhihua199","wenzhou123","wesley0924","wew35201314","weyasixx","wfc5000","wfdzsms76","wfs319","wg0818","wg11026","Wg114114","wg9456","wganchne","wh119639658","wh634349070","wh94038517","whan19940817","whbzll","whlrein","whnlovecbj99","whpoyou1745","whxwwww","why12444","why2262995","why5858518","whydjayljj","whyvaio","wi1dwi1d","wibeking","wibnh142231","wickzhou","wifeDS","wifi1995","wildboar","williamchgfg","willweidong","windflowers","wing0602ta","wingzhuzhu","winha1989","winmask","winner66","winner99","winnerzb13","winniesimon","winterfaye","winterkaka","wiselee","witch8","witty2012","wiwi217","wj.plum","wj12025","wj1225","wj14ydj2013","wj17b201","wj1982","wj19820","wj19898521","wj3753122","wj4000","wj4352","wj5375533","wj55183300","wj86441372","wj8720181","wj8743","wjcwoaini","wjd123","wjddlf43","wjdwjp","wjhm19861124","wjiaming","wjljj5210","wjly810417","wjly811009","wjm378614910","wjnj5210","wjp498091245","wjq0135","wjqc677","wjs7816369","wjsybswl","wjt690515290","wjvsxss","wjw0518","wjwggwtm","wjwj8913436","wjx110","wjx757303003","wjxio520","wjymq1987","wjzdjqzd","wjzxl217","wk871016","wkan0406","wknmwsw","wksos009","wl199328","wl279261732","wl425546267","wl5213344qq","wl546530","wl56804286","wl799715504","wliuwen521","wlk1314","wLLL080107","wlt2486","wlxxzx","wm1223521","wm82475","wm870829","wm931223813","wmee321","wmjzdbshjs","wml3883","wml9899","wmwyga888","wmz356","wmzjdyd123","wnmyun","WO0110","wo017546","wo1983","wo222813","wo3012583","wo453234000","wo471822284","wo59294523","wo64096098","wo654281","wo767848739","wo87540384","wo90430","wo90430.","woaeugene","woai12345","woai6316650","woaibaobei","woaigaoyun","woailovely","woaini32","woaini3695","woaini40","woaini4040","woaini8187.","woainidjq1","woainilo","woainimur456","woaisunnan","woaiwoma","woaixudong","woaiyan222","wobuquni1","wocanima361","wocaonimaqq","wochaoai123","wodashe","wode13528","wofengle96","wohaoxnihyb","wohua1987121","wohuaye","wojiaosharen","wokao1122","wolaidodo","wolaiye","wolf1117","wolijing","womeiqianle","wonderever","wonshi2009","wormbyaxx","worwang7","woshidage","woshifug","woshihuairen","woshihuluobu","woshilushuca","woshiren99","woshuo51314","wosixzl1","wow123wow","wow222","wowanwode","woxinrushui","wp5024641","wpaixh","wpnanjing","wpsa251673","wpwperp","wq16161","wq1986226","wq19890613","wq19910311","wq379823045","wq6571988","wq9997","wqh81484417","wqijianbo","wqq637","wqshinide888","wqx451719448","wqy1206","wr0045","wr184026269","wr362890211","wrj0578","ws0427","ws284833185","ws609003650","ws9792012","ws97988","wsad1234","wsad68839811","wsahahsw1","wsb19820824","wsikor","wskrgwrg","wskykings","wslaoben","wslrb1","wsm00000","wso4641777","wspkjl","wsq194","wsq253773","wsqd110","wss010","wss0448","wss3353","wss660812ns","wstaoyuan","wsw123456","wswxyf","wsx2008","wsxlovezx","wsy20120322","wsyjb123","wsywj001","wt0001","wtb850125","wtfl930820","wto123","wtp6320","wtw6225019","wtwow8","wu00439165","wu1539723","wu515610","wu5778987w","wu7232997","wu8851302","wualong","wubiao419","wubinshishen","WUCAIYUN","wuchen11","wuchuan520","wuchuan83","wuchun","wucj42bb","wudi5522551","wudong0320","wufuyangan","wuge421173","wugege","wuhao1111","wuhedong811","wuhen225","wuhonglei921","wuhui123456","wujian5555","wujiaping","wujiejie","WuJing48","wujingwen012","wujunhao","wujunlin","wuleimeng","wuleting","wuliangjiu","wuliangwu","wulihao6856","wumeier","wumin881128","wunaishengun","wunanhai","wunking","wupomm","wuqian","wuruidong18","wuruizhi0828","wushao2610","wushouhui","wuwanyao","wuwei1696","wuwu1980m4","wuwu1980m6","wuxuedong123","wuya123","wuyiran78951","wuyu1142141","wuyu500","wuyuetc100","wuyuxiao","wuzhe0923s","wuzhe0927s","wuzhian007","wuzhian1","wuzhimin","ww0224","ww10720","Ww1233552","ww416440939","ww569892237","ww5900","ww757429235","wwc123123","wwc123456","wwdtscy","wwe787001153","wwhxwf","wwj3707fss","wwjqqq","wwm2012","wwq13542","wwrczhang","wwsseeee","www001","www5213","www7403000","www757429235","www921390763","wwwtdy","wwyCOM","wwz13542","wwz19930320","wwzsuj","wx07921114","wx1390595270","wx191217017","wx20121010","wx2012914","wx201314","wx2159","wx22286","wx2883088","wx294491220","wx294586769","wx406907864","wx56821","wx568903192","wx615620","wx773278656","wx8269241","wx890122","wx893869","wx8989","wx9430071","wxb010613","wxc789545620","wxchcxy","wxf19910317","wxfy123","wxk1234","wxl520134qt","wxl8023qt","wxm6500","wxrswf","wxskyk","wxy19870812","wxzhangpeng","wy1024321958","wy3398318","wy343457880","wy499792119","wy87228","wy880316","wy998877","wyb198","wyb198198","Wyb1991","wyf0001","wyf592401280","wyf8882","wyh511819","wyjsmctz","wyl1990","wylovezj","wyq1234","wyt510","wytyhz","wyuan3","wywlf0729","wyx7777666","wz0428","wz6887888","wz86588395a","wza123","wzd19841116","wzflxl321","wzhongb","wzjfzl","wzkzwl","wzl5133","wzq0922","wzq537","wzq6382827","wzq888888","wzx659998","X1029840010","x105330","x112900","x1234567890","x125684967","x1366461333","x137238032","x184600","x1964755157","x19721022","x1991715m","x199403","x25753328","x279241241","X2810821088","x3213721","x344443","x366328017","x392790953","x496389091","x501168608","x510900511","x5427433","x5500952","x869398496n","x9484251","xacnet2","xaoyaomm","xavierxh09","xb198939","xbcder","xbzml520","xcgcxg136826","xchaini","xchcikege","xd19721017","xdbcx8","xdk5552004","xdvan1","xdzl1121","xf282637375","xfx25941","xfzhq111","xg756392443","xgd123456","xglb11","xh01xh","xh19911015","xhgbay","Xhh520","xhh710","xhhzs1991","xhhzs1992","xhk1314","xhx0721","xhzxylfnbj","xhzxylfnbja","xhzxylfnbq","xi0211","xia448196310","xia891114","xiabo1005","xiabowen","xiachenyi","xialongyunzp","xianboxb","xiandong0","xiang0311","xiang0510","xiang1iu","xiang6hu","xiang7247157","xiangrikui52","xiangyaliang","xianshibumei","xiao158777","xiao17378730","xiao19841028","xiao198907","xiao3540","xiao59934","xiao81593421","xiao886","xiaoai521","xiaoaiai521","xiaoanda","xiaobaolin2","xiaobaolin88","xiaobin","xiaobinbin","xiaobuwen","xiaochai","xiaochao4420","xiaochen526","xiaocong0904","xiaodangle","xiaodiansos","xiaodingdan","xiaodoudou","xiaofengdxf","xiaofutou","xiaofuzhon","xiaofuzhon1","xiaofuzhong","xiaofx222","xiaogaoqin","xiaoge1","xiaogewudi","xiaohai686","xiaohao1001","xiaohonghong","xiaohuaiguai","xiaohuijie","xiaohuiying","xiaojing_up","xiaojinwei","xiaojun52013","xiaoke69","xiaokudi","xiaole76","xiaolin1","xiaopababa","xiaopei794","xiaoqi21","xiaoquan2746","xiaoren","xiaoru2901","xiaosheng95","xiaoshuye112","xiaosijun","xiaotaiyang","xiaotan1314","xiaotan520","xiaototo12","xiaotrust","xiaowei9394","xiaoweity","xiaowo1211","xiaoxiao321","xiaoxiao520","xiaoxiaobaob","xiaoxie123","xiaoxiong20","xiaoxu1314","xiaoya1989","xiaoyao1010","xiaoyaofeng","xiaoyaoxuana","xiaoyu1240","xiaoyun914","xiaoyuqi1590","xiaozhuke","xiaozhussd1","xiaozhussd2","xiaxia521125","xiaxiaoge","xiayijie95","xiayirou","xiayun225516","xiayure2012","xie1114","xie2123221","xie46432115","xie692256098","xie7280504","xie846129","xie850406","xiedaxuan","xiedushen","xiedy0","xieei620","xiefulin","xiehaikun","xiehongjuan","xiehuik521","xiekai199206","xiekete","XieLei","xiemeng500","xieqihua","xiequan521","xierenren1","xierenren2","xierui123","xieshixian01","xieyu147258","xiezaoxin","xigua12345","xigua123456","xileo0","XIMENDAXUE","xin19880712","xin745243932","xinbingqi","xinbingqi168","xing198726","Xing823823","xingfu123","xingfu520","xinghai053","xingjm","xingkaiqiang","XingMao","xingok615","xingren1289","xingtong","xinguangak","xingyu79320","xinhui07","xinli2012","xintong3152","xinxin007007","xinxin338","xinxin851","xinxufeng168","xinzhe16888","xiongbing159","xiongj2008","xiongkai1000","xiongky","xiongwei0513","xiongwenkuan","xiongyi37","xirishusheng","xiutian61","xiwenbin00","xiwu555","xixi520520","xiyu72732517","xiyuming","xj1981814","xjg521","xjj520","xjktml","xjl1234","xjl1818","xjl331023","xjliuwei","xjlshelby","xjx101214","xk1992","xk1995","xk64277032","xk64277034","xl198755","xl2401","xl254551500","xl306514","xl78652","xl8800000","xlg420791736","xm1674","xmankaka","xmlsss","xmqhust","xmz0813","xmz1987","xoox7012","xope11000","xoxoxoxo","xp1985","xq18177307","XQiang","xql520","xqq2182204","xs2733","xs290903320","xs294658750","xs58452014","xs992427","xsd577","xshzsxy","xsqczj2","Xss520","xsw734387736","xsxcvbnmkjhg","xsyd8988","xt174156075","xt5626005","xt880770","xtingting10","xtingwx","xu0011","xu1156452660","xu123456","xu1389900","xu19870210","xu1989527","xu4211","xu443483041","xu47132658","xu5200","xu591603252","xu602609416","xu6258","xu7612112","xu762388198","xu789987","xu793178922","xu9787","xuan1992520","xuangang","xuanhao0706","xuanxuan8205","xubinghui","xudong510107","xuehao0809","xuejian62801","xuejingli","xuexia847046","xuezengchun2","xugang134","xugen20","xuguangwei","xuhai123","xuheng321","xuhonglang","xuhua14","xuhua893829","xuhui888","xuj112","xujinhui","xujunhao","xulei880327","xulei888","xuliang8011","xululu1","xumeng199","xuna1983","xunfang","xung21","xupeisi","xuqi1989","xuqian0423","xuqian830423","xurongze","xus128","xushenxiang2","xushijun","xutao128128","xuweijun4","xuwenhao","xuwenhui","xuwl903301","xuwl903302","xuxiaobo","xuxiaofeng24","xuxin0529","xuxin1990","xuxin4042960","XUXU4507","xuxuxu","XuYang","xuyang93986","xuyifanwl","xuynxn","xuyun1116","xw19901112","xwd92898","xwkpxp","xwn5201314","xwx3213721","xwyluck","xx112113114","xx5201314bly","xx520abcd","xxamm1314","xxb2sl3344","xxdn520","xxdn521","xxdn523","xxg30010","xxh3838","xxh406498141","xxhyf12345","xxkoolxx","xxl7703242","xxliaowu","xxm520zlf","xxoo011","xxpwx1986","xxvvzz111","xxx0629","xxxhjian110","xxxxx4949","xxxyyy339958","xxxzma","xy19930613","xy2gaokai","xy2kai","xy521qlj","xyc521","xyhazm","xyj520zq","xyk931","xyn211314","xyyanzi","xyylj1","xyz830523","xz230206","xzecom","xzj734387736","xzk420637002","xzm6677","xzq1986","xzq311","xzx520920","xzxtwt","xzxtwt1","xzy5213","xzz3839","y019841209","y1006325691","y1109727961","y123321y","y12345","y1260661130","y130325456","y1309758839","Y1314520","Y132564","y1741514","y176181812","y185544593","y315668","y3583772","y3711703","y524366626","y563643648","y71425440","y714254402","y7550838","y7788745","y823022","y83935539","y880602","YA1234","yah123","YaiC520","yan151617","yan19860215","yan19910715","yan414306","yan5ax","yanaihui","yanchrise","yang0508","yang119417","Yang12345","Yang123456","yang1314520","yang1851999","yang188059","yang2012a","yang320321","yang4151171","yang651203","yang66183","yang7892653","yang8chao8","yang911","yang9992","yangbo9026","yangfan1","yangfan1993","yangfang1010","yangfeifei","yangjian1972","yangjinming","yangjixch","YangJun","yangjunlin","yangjunlin1","yangkkk","YANGKUN","yanglei1993","yangLiang","yangliang1","yanglihong88","yangling","yangliu","yangmu1323","yangnuu","yangou","yangwanling","yangxie1","yangxiong78","yangxue521","yangyang1234","yangyang136","yangyuelin52","yangyusi520","yangze924","yangzhen0792","yangzhi1989","yanhua344","yanquanfu","yanran1314","yanrenshangg","yanshaohan","yanwei199442","YANXIONG","yanzhen8519","yanzi529","yanzi88","yao112233","yao6682991","yao81518","yaoamin","yaocun","yaodafeng1","yaojs1071","yaolie00","yaomok100","yaoniehudie","yaoniehudie1","yaoniehudie2","yaoniehudie4","yaoniehudiex","yaoniehudiez","yaoraooo","yaotian111","yaowen2080","yaoyao007","yaoyyj","yapaca","yartina","yas546336","yawei6398931","yaya00544","yaya520p","YB345998325","yb5582933","yb604396468","ybl0824","ybyaiziji","ybyqlxy","yc287192","yc6332336","yc7616653","yccxj396","ych08288","ycj5515","yck1228","ycl7216","ycps051031","ycruanzhou","ycs96555766","ycs965557665","yczlh1","yczlh2","yczlh3","yczlh4","yczlh5","yczlh6","yczlh7","yczlh8","yczlh9","yd900725","ydf8020","ydjabc","ydjabc01","ydk1996","ye123qiang","ye713358","ye891010","yeanping","yeefa1988","yeguanhui","yehun525","yejialan","yejs118","yejs1182","yemanmo","yemiaowei123","yening9500","yeqiang1987","yeqiquan1","yes8080","yesfsf","yeshibo","yeshibo1","yewutong","yeyouchenwh","yeyouchenwx1","yeyubiao","yeyubiao2012","yeyumeng","yezhenhui123","yezi1109","yf123001","yf123002","yf123003","yf123004","yf123006","yf123007","yf123008","yf123009","yf123010","yf1233005","yf12696","yf19900517","yf520888","yfd1226","yfd123","yfd129","yfd1980","yfd315","yg52609910","ygg0313","yghddxc","YGMKOKO","YGMPLA","ygnh730","ygq858314","ygy5280","yh1101921694","yh176168","yh390007969","yh420911012","yh6181541","yh66183","yh7559199","yh82168","yhd1989","yhfhr5188","yhh48577247","yhtlyh4","yhtlyh6","yhto147","yhzzb123","Yi3182121","yi861210","yibenwanli","yict21523","yidan8868","yidaoshanren","yilibing","yilijun1991","yinengda1","ying2780","ying45000","ying6677","ying88miao","yingshi","yingyuanbing","yingzi0328","yinhan520","yinhe1103","yinjichao290","yinjichao291","yinjichao292","yinkeyou","YiNKiSsu","yinmaihunzi","yinquan110","yinshengen","yinwei283720","yinweiwei","yinxin234","yinzhiqin","yipanxi","yipyip","yitiao2011","yiwangershen","yiyi2562","yj1110","yj19721202","yj821110","yjccfku","yjdsfq","yjgmieshi","yjh881012","yjm52088520","yjn17012","yjp800117","yjt1987n","yjy123","yjz870115","YKK777","ykn200612","yl0115","yl1992925","yl31378610","yl5613","YL58187598","ylalky","YLJ228660554","ylr1015","ym1588394717","ymf13140","ymhcyy","yml123","ymtaizj","ynbscn47","ynlxkgh","ynz888888","yoko1021","yoko2426","yoko4every","yong19840328","yong51741","yongjie8989","yongsheng213","yongtian6","you19891228","you221048","youabc","youbiao203","youcong","youfu123","youli101","younibishi","youttk","youyang369","youzldao","yp5723524","YPF520","ypl64981122","yqh0326","yql176168","yr19890708","yrl076217","yrqwer","yrz317","ys1199","ys55520037","yshao13","ysn123456","ysz612772","yt95903","ytcjjp","ytizccxx","ytr20060404","ytxwxq","yty20081204","yu0432","yu09wei6786","yu102723","yu1757","yu1757yu","yu1962403636","yu19821125","yu28911","yu3841","yu544616524","yu5535919","yu6485","yu652593230","yu74110","yu745243932","yu8523417","Yu96128","yuaixuelian1","yuaixuelian2","yuan1217","yuan1984","yuan3068","yuan7371","yuandatouhui","yuanhang88","Yuanjia7","yuanjie256","yuanlei0519","yuanmao115","yuanwu","yuanxiumei","yuanxuanlove","yuanyang888","yuanyi521","yuanzhenfu","Yuanzhiliang","yuanzihan000","yubai411","yubingyu","yudiao110","yue55044390","yuechuan2","yuedan520","yuejuan","yuelibing","yueming8889","YUEWENTAO","yueyue615","yueyue790202","yufan0824","yuhang211233","yuhao123456","yuhong870207","yuhong8727","yujian240","yujianxiang","yujinbu83","yujing199","yuki6638","yuliangliang","yulongji","yumen04141","yumi221","yumiko77","yun19860913","yun3221","yun736","yuncai0078","yunhaoyu","yuning242656","yunluo","yunluo520","yuqiang0526","yuqiangbin55","yushi2379","yushixiaopg","yusml11","yuwei32111","yuwei81666","yuwei88166","yuweixexie52","yuwowuguan","yuxiang2","yuxiao102723","yuxin1125","yuxin90","yuxing1756","yuxing7717","yuxuecheng","yuyaoxsh","yuyaxi012","yuyi007","yuyong001","yuyuyujun214","yuzhe2011222","ywlijin","ywq123456","yws397693543","ywtrdr","yx119911","yx503716334","yx636616","yxb54599","yxc940213","yxf5299988","yxg1986qq","yxh1983","yxl19850702","yxl850702","yxpswm1314","yxqzxh","yxt19931011","yxty000","yxty5132","yxx120411","yy198721","YY200714920","YY592050222","yy598788689","yy644126","yy666666","yy675704008","yy6962351","yy71425440","yy874172","yy901214","yy99971","yyalang","yyazz940205","yyb123","yyb7758","yyc83036035","yychaoa","yydyy1115","yyfsjn","yyloveme","yynnn610648","yyoo123","yyq168","yyq2012.9","yyx676592671","yyy1757","yyy1757yyy","yyyiii111","yyyouyou","yyyuuyyy","yyzhh520","yz3721","yzd1121","yzdvsyzl3","yzf19901025","yzg007","yzi0912","yzk920817","yzs719","yzs719000","yzs7190000","yzw5201314","yzxnh8","yzxnh9","yzy780127","yzy911030","yzzs215","z100001286","z100417755","z1010496997","z1016889","z1161657300","z1264567","z13394646060","z1339784953","z13911277940","z15969716444","z174389007","z176535367","z178112217","z178563357","z18646658079","z19062105","z19890318","z19921207","z2215921585","z237710828","z2401381","z269902316","z2718223","z27447808","z303266006","z307747133","z3343747","z33563","z342305319","z353982933","z358435363","z362731449","z363873871","z401053535","z413698721","z420228302","z430037434","z434312306","z461443444","z462190257","z495502922","z499737455","z5011124","z525858m","z532003114","z532423989","z547172384","z549654725","z570432315","z573193381","z5990099","z609868667","z6241023","z6677885","z7381456","z7486187","z766868581","z7690281","z77582458","z782355824","z799760432","z8181331","z845337470","z8700228","z8854454zxt","z909053733","z942034555","z9489468","za112512","zaiaideguodu","zak123","zanglei61","zao1986624","zaq1122","zaq1985122","zaq7690281","zay2012","zayushbc56","zayushbv56","zb2688","zbh1111","zblzgq111","zblzgq123","zbw100859","zc09113","ZC19930725","ZC243351373","zc277786695","zc33233","zcdelove13","zcl123","zcla12315123","zcqdb02","zctlove","zcx60222675","zcz739284868","zd95903","zdbtx538","zdd2015","zdpfanou","zdpfanou1","zdpfanou3","zeisss","zeng0213","zeng235123","zengyu","zeroal","zerosyc","zetra02","zewss520","zewss521","zf05531088","zfq123456","zfq465237","zfqjrppaa","zfz5186203","zg3670924","zg96918","zgd123456","zgd1992822","zgdtdh","zggjbwgbwcwg","zghj513","zghj514","zgr0803","zgskqy","zgzh5151","zh10127","zh101278","zh19940210","Zh20059025","zh25959721","zh321zh321","zh334455","zh362531","zh414323","zh493863324","zh520sj","zh86742090","zh88866","zhabg33","zhai1987","zhai2012","zhaiqunfeng","zhan754","zhan8866","zhang0211","zhang0620","zhang08839","zhang116688","zhang1391127","zhang14256","zhang199069","zhang2233","zhang2wa11","zhang3085","Zhang5026042","zhang5125000","zhang5385077","zhang59864","zhang6325565","Zhang6667105","Zhang721","zhang8681041","zhangchi106","ZHANGCUN","zhangdawd","zhangdazui12","zhangdd","zhangfei619","zhangguihao","zhanghang542","zhanghaozi13","zhangheng1","zhanghongye","zhanghuan","zhangji12315","zhangjie520","zhangjin12","zhangjin23","zhangjin99","zhangjing111","zhangjun2580","zhangjun7655","zhangkai88","zhangkang008","zhangliang86","zhanglisen","zhangmaomy","zhangmin826","zhangming821","zhangniu929","zhangrong78","zhangsen","zhangshan888","zhangshansha","zhangshen","zhangshen33","zhangshiyang","zhangtai11","zhangtai2000","zhangtao1986","zhangtete","zhangtieyan2","zhangtieyan3","zhangtieyan4","zhangtieyao","zhangwei5699","zhangweiqa","zhangwoker","zhangxiaomei","zhangxing998","zhangyuhua","zhangzhe2","zhangzhen112","zhangzhen198","zhangzhentao","zhangzheqwe","zhangzhiyu","zhankjl04","zhankun0429","zhanlichao17","zhanlong6246","zhanlongd","zhanlongda","zhanlongdapa","zhanmingyang","zhao0510","zhao0518","zhao121118","zhao1688","zhao182ligai","zhao19860215","zhao205","zhao5017809","zhao58160","zhao6000000","zhao628789","zhao6549","zhao65491","zhao7950468","zhao8258177","zhao873109","zhaobiaoa","zhaohai1347","zhaoheng321","zhaojingjing","zhaoli24","zhaolianwei","zhaopei97","zhaoqiyong","zhaoshanmei","zhaoshaoyuan","zhaoteng000","zhaotenghui","zhaowei203","zhaoxuan123","zhaoyunwen","zhaoz1hao","zhaozhen112","zhaozhen2012","zhaozhibin","zhc123","zhcx1329","zhdonlian","zhe101500","zhen001","zheng2012","zheng5320923","zhengdayou1","zhenghao110","zhenglin0155","zhenglin0255","zhengsiping","zhenzhen888","zhf791011","zhf886","zhfct123","zhg1991","zhiaixiao","zhiaiyao","zhichuanpin1","zhichuanpin3","zhijiage","zhiqing758","zhirong21","zhirong22","zhiweiwx1","zhiying123","zhiying1993","zhiyuan320","zhjzhj123","zhl520cl","zhll1980","zhnsgs","zhong11119","zhong119","zhong2011","zhongar","zhongguo2012","zhonglei002","zhonglj3","zhongqixian","zhongribin1","zhongsy777","zhongxi","zhongyao5","zhongyx","zhongzhuang","zhongziyi0","zhope1987","zhou100904","zhou110","zhou120jing","zhou422323","zhou652489","zhou661818","zhou87302983","zhou881216","zhou908012","zhou95736810","zhoufeng520","zhougee","zhouheng158","zhouhueif","zhoujay156","zhoujianping","zhoujiayu325","zhoujunyan","zhoulinguo11","zhousizhe333","zhoutao1","zhouweiming","zhouwenj","zhouxiaoshe","zhouxin1225","zhouxuan56","zhouyangjay","zhouyuhua68","zhouzhiang","zhouzhiyi","zhp20110427","zhp921101","zhqng2wa11","zhqqjjie","zht1289","zht1986","zhtjcc2013","zhu1391015","zhu168278","zhu3465447","zhu3893197","zhu500222","zhu50131881","zhu52000","zhu523472715","zhu6871872","zhu831013","zhuanghuzxc","zhubq1691","zhuchaoyu327","zhuchuanfang","zhudang180","zhudujiang","zhufa821116","zhufayi","zhuguoping","zhuguoqing55","zhujc3","zhujiang520","zhujun668182","zhujunfeng","zhukaisnake","zhul1988","zhulianggao","zhurengong","zhushaojun","zhushoudong","zhutou13","zhutouboy","zhuwei3333","zhuwenbo119","zhuyu890622","zhuyuqing80","zhuzhentao","zhuzhichao","zhuzhu222","zhxib500234","zhyang520","zhzlzj","zi19810615","zi307537413","zibin1994","zielstrebig","zihao123","zijing8756","zipair","zipair01","ziwu0212","zixing8691","zixinglian","ziyunaxl","zizoloziz","ZJ0123456","zj1001","zj19811016","zj356252190","Zj5431187","zj58yd","zj59127216","zj77414950","zjbkuangyh..","zjctyf","zjdth7749","zjeqkzl7777","zjf1989","zjfr2030","zjfyzxm","zjg69339822","zjgxf317","zjh102001","zjh123321","zjh1425","zjh619265853","zjh821116","zjh9621","zjiloveme","zjjxjcy","zjk19930103","zjl1992","zjlll600","zjnb0211","zjoylee1","zjp565102143","zjw1988802","zjxking","zjxking2","zjyeweiwei","zjyszg","zjz0605a","zjzjzj","zkw1989","zl406976280","zl4444","zl478597918","zl62398","zl800303","zl8767","zl8872416","zlcai7520","ZLF520953","zlf520xxm","zlh792566004","zlj0923","zlj35053","zlz861026","zlzl80123490","zlzw997688","zm2973933","zmaibaobao","zmk8901","zmliwyq123","zmq438888716","zms1989","zms19891015","zmtmx2","zn1510060070","zn1985720","zn3235558","zn596259864","zolitmm","zombie3011","zongwei","zore1999","zore520","zou6001","zoukun","zouminsheng","zouweibo0o","zp3131520","zp327339756","zp890520","zpp2jt1314","zq002002","zq1234567","zq199257","zq19930228","zq329a","zq520a","zq526551258","zq810477956","zqh210","zqmaohh","zqshldq21","ZQY1127","zqy837127192","zr217367","zr779694","zrgamer419","zrh875609265","zrokzrok","zrtkell","zryywpppp","zs12345","zsd52119","zsemnb","zsg1975","zsh111","zsj930703","zsjaihwx","zsk355588684","ZSQALX","zsqing1120","zsssjing","zsssquan","zswo2ren","zsx119119","zsx850421","zsy304046025","zsy593185331","zsyh2012","zt0328","zt2224823","ztl521","ztm009","Zudeep","zui0227","zuiaifei","zuiwanyici","zuo860113","zuochencen","zuyu159357","zw7896863","zwd07712","zwhlyl","zwj698829","zwl827900","zwp1980216","zww63151","zww6921042","zwy0623","zwy12345","zx1981100","zx198908","zx203400","zx448447888","zx454654797","zx578082884","zx6431216","zx666987","zx810621","zxc1025141","zxc123z1x2c3","zxc2216","zxc49337295","zxc600215","zxc8899","zxccxzzxc","zxcvbnm1069","zxcvbnm4518","zxczxczaq","zxgg1984","zxl1019","zxl68153436","zxll520530","zxm243926524","zxm47313420","zxm610490443","zxm7758258","zxmvsdkj","zxqaizhu","zxtzxtzxtlov","zxww6152433","zxx199267","zxy0429","zxym21","zy2004216","zy316188093","zy499613097","zy551731555","zy56875999","zy5840538","zy790323","zy86843041","zy890622","zybulibuqi","zybzyb","zyf2828","zyf4848","zyf4869","zyf6868","zyf7878","zyfxxh3838","zyfxxh4848","zyfxxh6868","zyfxxh7878","zygzxn999","zyh962","zylz12345","zyrwso","zyt1219","zyt134017629","zyt841114","zyw133399995","zyy572038","zyylaj","zyzonoo","zz1004","zz23698745","zz251740018","zz494334984","zz555q","zz56821","zz5874780","zz619421345","zz6721104","zz67211044","zz8832131","zz96217","zzc562521427","zzcvvv","zzd520ysy","zzg622","zzh52541","zzh7758521","zzhgas","zzipshuai8","zzj123456789","zzj200008","zzj5240","zzjlovehtt","zzjyxh","ZZJZZL","zzkdc24k","zzkkww","zzlaiyll","zzoouu11","zzphyy","zzq19900102","zzt119","zztsnmkf","zzw1016","zzxh119","zzy0702","zzy512400","zzy512402","zzy512406","zzyy883155","zzz1004","zzz222","zzzwing","zzzym888","zzzzcb43","zzzzcb46"
};
List<ClientAccount> clientList = new ArrayList<ClientAccount>();
long now = System.currentTimeMillis();
ArrayList<String> notLogin = new ArrayList<String>();
for(String username : usernames){
	boolean has = false;
	clientList = em.query(ClientAccount.class,"username = '"+username+"' or clientId='"+username+"'","",1,1000);
	if(clientList != null && clientList.size() > 0){
		for(ClientAccount client : clientList){
			if(client.getLoginTimeForLast().compareTo( new Date(now - 13*3600*1l*24*1000)) > 0){
				out.println(client.getUsername()+"\t"+client.getLoginTimeForLast()+"<br/>");
				has = true;
				break;
			}
		}
		
	}
	if(!has){
		notLogin.add(username);
	}
}
out.println("---------下面是11月1日后没有登录过的------------------------------<br/>");
for(String str : notLogin){
	out.println(str+"<br/>");
}

	
%>
<%@page import="com.fy.gamegateway.stat.StatManager"%>
<%@page import="com.xuanzhi.tools.simplejpa.SimpleEntityManager"%>
<%@page import="com.fy.gamegateway.stat.Client"%>
<%@page import="com.fy.gamegateway.stat.ClientAccount"%><html>
<head>
</head>
<body>

</body>
</html> 