Spring-How-To
=============

Spring, простые примеры как нам жить с этим фреймворком
Accessing System Properties in Stack 3

Stack Links

    LDS Java Stack Home
    Reference Documentation
    Stack SharePoint Site
    Stack Support Forum
    Community Jira
    Community Sonar
    Roadmap
    Twitter Feed
    Launch Stack Starter Stack-project-16.png
    Java Stack Training Updated.png 

Contents
 [hide] 

    1 Using System Properties Instead of JNDI Properties (for Stack 3.2+)
        1.1 1. Accessing System Properties via Spring Expression Language (SpEL) Expressions
        1.2 2. Accessing System Properties via Property Placeholders
        1.3 3. Accessing System Properties using the Spring Environment Abstraction
    2 Using JNDI Properties (Stack 3.0.x and 3.1.x)
        2.1 Steps
        2.2 Usage
        2.3 Explanation (the long version)

Using System Properties Instead of JNDI Properties (for Stack 3.2+)

If you are using Spring 3.1 (Stack 3.2 and above) you can use system properties instead of JNDI properties. The Stack provides three options:
1. Accessing System Properties via Spring Expression Language (SpEL) Expressions

Annotate the property with a @Value annotation containing the SpEL expression shown in the following example:

@Value("#{systemProperties['foo.bar']}")
private String fooBar;

Replace the property name in the expression with the property that you want to field to contain after initialization.
2. Accessing System Properties via Property Placeholders

Add a propery placeholder to your applicationContext.xml configuration file (NOTE: the system-properties-mode of OVERRIDE does not seem to be necessary, ENVIRONMENT, the default, works just fine):

<context:property-placeholder system-properties-mode="OVERRIDE" .../>

Consume the system properties as follows:

@Value("${foo.bar}")
private String fooBar;

Note that you can also supply a system property via the @Value annotation on a setter method. See the Spring Javadocs for the @Value annotation and the Spring usage documentation for <context:property-placeholder> for more details.
3. Accessing System Properties using the Spring Environment Abstraction

The final way to access system properties is through the environment abstraction provided by Spring. First, inject the Environment object into the class where you would like to access system properties:

import org.springframework.core.env.Environment;
 
@Autowired // or @Inject
private Environment environment

Then, access properties on the environment object using the applicable getter methods:

String someUrl = environment.getRequiredProperty("some.url"); // will throw exception if value does not exist
 
String name = environment.getProperty("aName", "Bob"); // uses the default name of "Bob" is value does not exist
 
Long portNumber = environment.getProperty("host.port", Long.class); // converts string property to type, if possible.

This method works well if you have a variety of properties to access, or you need to be more flexible in how you use the properties. For more information, consult the Springsource blog entry at http://blog.springsource.com/2011/02/11/spring-framework-3-1-m1-released/ discussing this feature.
Using JNDI Properties (Stack 3.0.x and 3.1.x)

For properties that do not need to be encrypted you can simply follow the instructions for Stack 3.2+ above. For decrypted properties keep reading this section, and consider whether you want to make all your properties JNDI properties for the sake or consistency, or if you want your non-encrypted properties to be system properties for the sake of simplicity. Prior to version 7 of Tomcat, decrypting properties was performed somewhere between where system properties became JNDI properties and therefore you have to send encrypted properties through the JNDI property declaration and configuration. In Tomcat 7 (therefore Stack 3.2+) changes were made that allowed decryption of system properties.

In Stack 3 we have removed application properties of Stack 2, and instead recommend using JNDI for defining application specific properties. This can be accomplished by adding the configurations outlined in the following steps:
Steps

    deploy/src/main/resources/server.xml 

        <GlobalNamingResources>
            <Environment name="PetstorePurchaseUrl" type="java.lang.String" value="${petstore.purchase.value}"/>
	    ...
        </GlobalNamingResources>

    deploy/src/main/resources/*.catalina.properties 

        petstore.purchase.value=whatever/Services/petPurchase

    web/src/main/webapp/META-INF/context.xml 

        <ResourceLink name="PetstorePurchaseUrl" global="PetstorePurchaseUrl" />

    web/src/main/resource/META_INF/spring/applicationContext.xml: 

        <jee:jndi-lookup id="petstorePurchaseUrl" jndi-name="PetstorePurchaseUrl"/>


Usage

    With this setup, you can inject this into any of your Spring beans, or use it directly in a JSP, ... with something like: 

        @Resource(name="petstorePurchaseUrl")
        public String petstorePurchaseUrl;

    Where the name in @Resource matches exactly the id of the jee:jndi-lookup declaration. 

    or in JSP (after exposing it like with model.setAttribute("petstorePurchaseUrl", petstorePurchaseUrl) if using Spring MVC): 

        $.ajax({
                cache: false,
                url: "${petstorePurchaseUrl}"
        	...
        });

Explanation (the long version)

Since there is one server.xml file per Tomcat instance, by defining an environment resource in server.xml with something like: <Environment name="PetstorePurchaseUrl" type="java.lang.String" value="${petstore.purchase.value}"/>, you are exposing a property named PetstorePurchaseUrl to every application that runs on that Tomcat server. By making the value parameterizable - i.e. by specifying a value like this: value="${petstore.purchase.value}", this allows the property to be changed per environment. For instance, you could put one value for petstore.purchase.value in dev.catalina.properties, and a different value for petstore.purchase.value in continuous.catalina.properties, ... Then, when the application is deployed to a given environment, the resulting catalina.properties file for that environment will be deployed with the application, and thus the correct value for that environment will be exposed.

The ResourceLink in context.xml is a mapping between the global property exposed in server.xml (and available to all applications that run on the server) and the specific web application. For instance, if for a given application you wanted this JNDI property exposed to the application by a different name (say PetstoreUrl, instead of PetstorePurchaseUrl), you could place the following in your context.xml - <ResourceLink name="PetstoreUrl" global="PetstorePurchaseUrl" /> and then the global property PetstorePurchaseUrl would be available in the given application via the name PetstoreUrl.

Then the Spring configuration in applicationContext.xml <jee:jndi-lookup id="petstorePurchaseUrl" jndi-name="PetstorePurchaseUrl"/>, just exposes the PetstorePurchaseUrl under the given id for simplified injection and access by the application. Alternatively, if you did not want to use this Spring helper, and wanted to just use native Java, to get the JNDI variable, you could leave <jee:jndi-lookup id="petstorePurchaseUrl" jndi-name="PetstorePurchaseUrl"/> out of your applicationContext.xml and then just do something like the following in code.

        String petstorePurchaseUrl = null;
        try {
        	Context initCtx = new InitialContext();
        	Context envCtx = (Context) initCtx.lookup("java:comp/env");
        	petstorePurchaseUrl = (String) envCtx.lookup("PetstorePurchaseUrl");
        } catch (NamingException e) {
        	e.printStackTrace();
        }

There are some shortcuts to the steps listed above. For instance, you could just skip the definition in server.xml entirely and put an <Environment ... declaration in context.xml instead, and work from there. However, when defining data sources, we have run into issues with this approach, and so to maintain consistency we just recommend the long way for defining all JNDI properties. Not to mention, in Tomcat 7, we believe that there will be changes to how JNDI properties are defined, and we think that the steps above are the best approach when planning for the future.

Additionally, if you wanted to be entirely formal, you could add the following to your web.xml so that someone deploying the application could know that values would need to be provided for those values (if you did not specify them in your prod catalina.properties):

    <resource-env-ref>
        <resource-env-ref-name>PetstorePurchaseUrl</resource-env-ref-name>
        <resource-env-ref-type>java.lang.String</resource-env-ref-type>
    </resource-env-ref>
