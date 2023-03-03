# Présentation Générale

XPE : XML Processing Engine.
This project makes possible to describe, in a simplified xml format, a sequence of XML processing steps.


## Lancement 

### Command line

Command line launch based on Apache Commons CLI : 

```

java -jar xml-processing-engine-dist-1.00.00-jar-with-dependencies.jar -h

-h,--help                                The help
-P <arg>                                 The params of he XPE script
-xpeUri,--xmlProcessingEngineUri <arg>   The uri of the XPE script

```

```

java -jar xml-processing-engine-dist-1.00.00-jar-with-dependencies.jar -xpeUri main.xpe -Pparam1=value1 -Pparam2=value2

```

### JAVA code


```

ProcessingStep proc = new ProcessingStep();
proc.load("main.xpe");
Map<QName, Param> params = new HashMap<>();

params.put(new QName("param1"), new Param(new QName("param1"),
			new XdmAtomicValue("value1")));
params.put(new QName("param2"), new Param(new QName("param2"),
			new XdmAtomicValue("value2")));

proc.execute(params);

```

### Example

see src/test/examples

```

java -jar xml-processing-engine-dist-1.00.00-jar-with-dependencies.jar -xpeUri src/test/resources/examples/xpe/foreach3.xpe -Pparam1=value1 -Pparam2=value2

```


```

java -jar target\xml-processing-engine-dist-1.00.00-jar-with-dependencies.jar -xpeUri src/test/resources/examples/xpe/xslt-pipe.xpe -Puri1=file:/.../src/test/resources/examples/data -Puri2=file:/.../debug -Puri3=file:/.../out1 -Puri4=file:/.../out2

```




## Déscription

XPE is based on [Saxon](http://www.saxonica.com/). By default, its uses the HE edition but it is possible to use EE or PE edition juste by importing the necessary packages via the projet POM. 

XPE is an XML scripting. 
By combining simple algorithms, XPATH and XSLT/XQUERY, this tool makes it possible to create structured data processing chains in a simple and intuitive way.

For example : 
```

<?xml version="1.0" encoding="UTF-8"?>
<processing xmlns="http://com.sitc.xml.processing.engine.org/config"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xmlns:file="http://expath.org/ns/file">

	<!-- saxon config file -->
	<config uri="'../conf/saxon/processing.xml'"/>

	<!-- launch parameters -->
	<param name="conf-file-path" select="()"/>
  
  <message source="'source'" select="file:path-to-uri($conf-file-path)"/>	

</processing>

```

In this example we can : 
- Deal with saxon config
- Passe a parameter called **conf-file-path** to this XPE script
- log a message with an xpath représenting the URI of the parameter **conf-file-path**

## Steps


- **param**, **option**, **variable** : steps to define a call paramter, a processing option or a simple variable . 

```

<param name="conf-file-path" select="()"/>

<option name="option" select="1"/>

<variable name="sequence" select="(1,2,3)"/>

```

- **message** : step to hire a log event  *MessageListener* : the default one is a log4j logging. It can be changed for any other need 

```

<param name="msg" select="'default'"/>
<message source="'source'" select="$msg" level="'error'"/>

```

- **if**, **choose** :  

```
<if test="true()">
    <message source="'source'" select="'la seqeunce est vide'"/>
</when>


<choose>
    <when test="false()">
        <message source="'source'" select="'la seqeunce est vide'"/>
    </when>
    <otherwise>
        <message source="'source'" select="'la seqeunce est non vide'"/>
    </otherwise>
</choose>

```

- **foreach** :  

```

<variable name="sequence" select="(1,2,3)"/>
    
<foreach name="i" select="$sequence">
    <foreach name="j" select="$sequence">
        <variable name="k" select="$i"/>
        <message source="'source'" select="concat('i = ',$i,', j = ',$j,', k = ',$k)"/>
    </foreach>
</foreach>

```

- **call** : step de call an other XPE scripting by its URI  

```
    
<call uri="'../message.xml'">
    <param name="msg" select="'Hello Word'"/>
</call>

```

- **read** : step de interact with a console  

```

<read name="value"/>

```

- **exit** : system.exit   

```

<exid code="99"/>

```

- **break** : break processing or looping   

```

<break/>

```

- **continue** : force loop to continue  

```

<continue/>

```

- **wait** : wait for period (ms)  

```

<wait period="1000"/>

```

- **return** : processing return value  

```

<return select="current-date()"/>

```

- **java** : step to call a java method with spécific signature  

```

<variable name="sequence" select="(1,2,3)"/>
    
<java class="com.test.JavaStepExample" method="method" param="$sequence" result="result"/>

```


```

import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;

public class JavaStepExample {

	public static XdmValue method(XdmValue value) {
		return new XdmAtomicValue(true);
	}

}

```

- **script** : script de call a shell script  

```

<variable name="sequence" select="(1,2,3)"/>

<script uri="'./script.bat'" args="$sequence"/>

```

- **pipe** : a pipeline of XML transformation  

  - ***xslt***
  
```
<message source="$source" select="'Start'" level="info"/>
	
<pipe
source="'file:/input'"
destination="'file:/output'"
source-file-name-variable-name="input-file-name"
destination-file-name="replace($input-file-name,'[.]xml','.1.xml')"
parallel="true()" 
threads="1" 
file-size-max="'1 ko'" 
file-name-filter="'.*[2-3].*'" 
dir-name-filter="'.*[2].*'" 
recursive="true()" 
debug="true()" 
debug-dir-uri="'file:debug'"/>
  <xslt uri="'../do.xslt'">	
	 <param name="date" select="$date"/>
  </xslt>
</pipe>

<pipe
source="'file:/input'"
parallel="true()" 
threads="1" 
file-size-max="'1 ko'" 
file-name-filter="'.*[2-3].*'" 
dir-name-filter="'.*[2].*'" 
recursive="true()" 
debug="true()" 
debug-dir-uri="'file:debug'"/>
  <xslt uri="'../do.xslt'">	
	 <param name="date" select="$date"/>
  </xslt>
  <pipe
  destination="'file:/output1'">
    <xslt uri="'../do1.xslt'"/>
  </pipe>
  <pipe
  destination="'file:/output2'">
    <xslt uri="'../do2.xslt'"/>
  </pipe>
</pipe>



```




  - ***xquery***
  
```
<message source="$source" select="'Start'" level="info"/>
	
<pipe
source="'file:/input'"
destination="'file:/output'"
source-file-name-variable-name="input-file-name"
destination-file-name="replace($input-file-name,'[.]xml','.1.xml')"
parallel="true()" 
threads="1" 
file-size-max="'1 ko'" 
file-name-filter="'.*[2-3].*'" 
dir-name-filter="'.*[2].*'" 
recursive="true()" 
debug="true()" 
debug-dir-uri="'file:debug'"/>
  <xquery uri="'../do.xq'">	
	 <param name="date" select="$date"/>
  </xquery>
</pipe>

```

- **try-catch-finally** : to deal with exceptions  

```

<message source="$source" select="'Start'" level="info"/>
	
<try-catch-finally>
    <try>
    	<message select="file:last-modified('toto.xml')" source="'source'"/>
    </try>
    <catch name="e">
    	<message select="'message'" level="'error'" source="'source'" throwable="$e"/>
    </catch>
    <finally>
    	<message select="'finally'" source="'source'"/>
    </finally>
</try-catch-finally>

```

- **config** : to deal with Saxon configuration. It can be done for processing, pipe and call steps.  

```

<?xml version="1.0" encoding="UTF-8"?>
<processing xmlns="http://com.sitc.xml.processing.engine.org/config"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xmlns:file="http://expath.org/ns/file">

  <!-- config saxon -->
  <config uri="'../conf/saxon/processing.xml'"/>
  
  <!-- paramètres de lancement : voir main.xml -->
  <param name="conf-file-path" select="()"/>

  <call uri="'../step1.xml'">
    <!-- config saxon -->
    <config uri="'../conf/saxon/step1-processing.xml'"/>
    <param name="conf-file-uri" select="file:path-to-uri($conf-file-path)"/>
  </call>	

</processing>

```