spookystuff
===========

(OR: how to turn 21st century into an open spreadsheet) is a scalable and distributed web scrapping/data collection/acceptance QA environment based on Apache Spark. The goal is to allow the Web being queried and ETL'ed as if it is a database.

Dependencies
-----------
- Apache Spark
- Selenium
    - GhostDriver/PhantomJS (for Google Chrome client simulation)
- JSoup
- Apache Tika (only for non-html parsing)
- (build tool) Apache Maven
    - Scala/ScalaTest plugins
- Current implementation is influenced by Spark SQL and Mahout Sparkbinding.

A one minute showcase
-----------
#### 1. Headhunting on LinkedIn
- Goal: Find highest-ranking professionals in you area on LinkedIn. whose full name is either 'Sanjay Gupta', 'Arun Gupta' or 'Hardik Gupta', print their respective full name, title and list of skill
- Query:
```
    (sc.parallelize(Seq("Sanjay", "Arun", "Hardik")) +>
      Visit("https://www.linkedin.com/") +>
      TextInput("input#first", "#{_}") +*>
      Seq( TextInput("input#last", "Gupta"), TextInput("input#last", "Krishnamurthy")) +>
      Submit("input[name=\"search\"]") !)
      .wgetJoin("ol#result-set h2 a") //faster
      .map{ page => (
      page.text1("span.full-name"),
      page.text1("p.title"),
      page.text("div#profile-skills li")
      )
    }.collect().foreach(println(_))
```
- Result (truncated, query finished in 1 minutes, test on wifi with ~400k/s download speed):
```
(Abhishek Arun Gupta,President & Senior IT Expert / Joint Chairman - IT Cell at Agra User Group / National Chamber of Industries & Commerce,ArrayBuffer(Requirements Analysis, SQL, Business Intelligence, Unix, Testing, President & Senior IT Expert, Joint Chairman - IT Cell, Quality Assurance (QA) & Automation Systems, Senior Automation Testing Expert, Senior Executive, Industry Interface))
(hardik gupta,--,ArrayBuffer())
(Arun Gupta,Sales at adjust by adeven,ArrayBuffer(Mobile, Business Strategy, Digital Media, Advertising Sales, Direct Sales, New Business Development, Mobile Marketing, Mobile Advertising, Publishing, Mobile Devices, Strategic Partnerships, Start-ups, Online Marketing, Mobile Applications, SEO, SEM, Business Development, Social Networking, Digital Marketing, Management, Digital Strategy))
(Dr. Sanjay Gupta,Co-Founder & Director at IMPACT PSD Private Limited,ArrayBuffer(Computer proficiency, Secondary Research, Operations Management, Qualitative Research, Research and M&E, Data Management, Data Interpretation, M&E, Research, Report Writing, Data Analysis, Proposal Writing, Program Management, Capacity Building, NGOs, Leadership, Market Research, Policy, Civil Society, International Development, Nonprofits, Public Policy, Corporate Social Responsibility, Training, Program Evaluation, Analysis, Business Development, Sustainable Development, Data Collection, Technical Assistance, Organizational Development, Fundraising, Community Development, Quantitative Research, Government, Program Development, Policy Analysis, Reproductive Health))
(Dr. Arun Kumar Gupta,Chief Executive Officer,ArrayBuffer())
... (75 lines)
```

#### 2. Find interchangeable parts of a washing machine on appliancepartspros.com
- Goal: Find all parts on model 'A210S', print the full name of the model, schematic description, description/brand/and number of each part, and all their substitutes
- Query:
```
    (sc.parallelize(Seq("A210S")) +>
      Visit("http://www.appliancepartspros.com/") +>
      TextInput("input.ac-input","#{_}") +>
      Click("input[value=\"Search\"]") +> //TODO: can't use Submit, why?
      Delay(10) ! //TODO: change to DelayFor to save time
      ).selectInto(
        "model" -> { _.text1("div.dgrm-lst div.header h2") },
        "time1" -> { _.backtrace.last.timeline.asInstanceOf[Serializable] } //ugly tail
      ).wgetJoin("div.inner li a:has(img)")
      .selectInto("schematic" -> {_.text1("div#ctl00_cphMain_up1 h1 span")})
      .wgetJoin("tbody.m-bsc td.pdct-descr h2 a")
      .map(
        page => (
          page.context.get("_"),
          page.context.get("time1"),
          page.context.get("model"),
          page.context.get("schematic"),
          page.text1("div.m-pdct h1"),
          page.text1("div.m-pdct td[itemprop=\"brand\"] span"),
          page.text1("div.m-bsc div.mod ul li:contains(Manufacturer) strong"),
          page.text1("div.m-pdct div.m-chm p")
          )
      ).collect().foreach(println(_))
```
- Result (truncated, query finished in 10 minutes, test on wifi with ~400k/s download speed):
```
(A210S,A210S Washer-Top Loading ,07-Transmissions Parts for Maytag A210S,Collar-Dri,Whirlpool,Y014839,Part Number Y014839 (AP4277202) replaces 014839, 14839.)
(A210S,A210S Washer-Top Loading ,08-Transmissions Parts for Maytag A210S,Collar-Dri,Whirlpool,Y014839,Part Number Y014839 (AP4277202) replaces 014839, 14839.)
(A210S,A210S Washer-Top Loading ,05-Suds Saver Parts for Maytag A210S,Screw, Strainer to Pump,Maytag,911266,null)
... (311 lines)
```

### Showcase environment
- The easiest way is to test locally on you scala IDE or REPL.
- You need to install [PhantomJS](http://phantomjs.org/) on you computer. The default installation directory is '/usr/lib/phantomjs'.
    - (If your PhantomJS is installed to a different directory, please change *phantomJSRootPath* in *org.tribbloid.spookystuff.Conf.scala* to point to your PhantomJS directory.)
- You DON"T need to install Apache Spark to test it in local simulation mode (where masterURL = "local[...]"), the spark jar in you local Maven repository has everything you need.
    - You DO need to do this to deploy your query or application to a cluster.
1. Git clone me: git clone https://github.com/tribbloid/spookystuff.git
2. Create your scala project. Add spookystuff-core to its dependency list
    - If you project is managed by Maven, add this to your .pom dependency list:
    ```
        <dependency>
            <groupId>org.tribbloid.spookystuff</groupId>
            <artifactId>spookystuff-core</artifactId>
        </dependency>
    ```
    - Alternatively you can just run Spark-shell or Scala REPL within the project environment.
3. Import scala packages and context, including:
    - org.apache.spark.{SparkContext, SparkConf} (entry point for all Spark applications)
    - org.tribbloid.spookystuff.SpookyContext._ (enable implicit operators for web scrapping)
4. Initialize your SparkContext ```val sc = new SparkContext(new SparkConf().setMaster("local[*]"))```
    - if some of your target websites are unstable and may need some reloading, use this:
    ```val sc = new SparkContext(new SparkConf().setMaster("local[${MaxConcurrency},${MaxRetry}]"))```
5. That's it, you can start writing queries, run them and see the results immediately. 

Query/Programming Guide
-----------
[This is a stub]

So far spookystuff only supports LINQ style query language, APIs are not finalized (in fact, still far from that) and may change in the future.

I'm trying to make the query language similar to the language-integrated query of Spark SQL. However, as organizations of websites are fundamentally different from relational databases, it may gradually evolve to attain maximum succinctness.

Cluster Deployment
-----------
[This is a stub] Theoretically all Spark application that runs locally can be submitted to cluster by only changing its masterURL parameter in SparkConf. However I haven't test it myself. Some part of the code may not be optimized for cluster deployment.

- Make sure PhantomJS is installed on all cluster nodes.

- If you want to write extension for this project, MAKE SURE you don't get *NotSerializableException* in a local run (it happens when Spark cannot serialize data when sending to another node), and keep all RDD entity's serialization footprint small to avoid slow partitioning over the network.

Maintainer
-----------
(if you see a bug these are the guys/girls to blame for)

- @tribbloid (pc175@uow.edu.au)