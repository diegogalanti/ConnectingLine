
# **ConnectingLine**

ConnectingLine an Android custom view developed in Kotlin. It consists of a line connecting two other views, defined as origin view and destination view. It is possible to define on which side the line should leave the origin view and on which side the line will reach the destination view. It is also possible to define visual characteristics of the line, such as color, thickness, shadow, etc. All definitions can be done programmatically or through XML attributes.

## Install

This library can be easily included in your project through the Jipack repository:

### **Grade:**

Step 1. Add the JitPack repository to your build file, add it in your project _build.gradle_ at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
For newer Gradle versions you can add directly to your _setting.graddle_ file:

	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Step 2. Add the dependency:

	dependencies {
	        implementation 'com.github.diegogalanti:ConnectingLine:v1.0.1'
	}

### **Maven:**

Step 1. Add the JitPack repository to your build file:

	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
Step 2. Add the dependency:

	<dependency>
	    <groupId>com.github.diegogalanti</groupId>
	    <artifactId>ConnectingLine</artifactId>
	    <version>v1.0.1</version>
	</dependency>
 
 ## Usage
 
 After including the library in your project you can add the view like in the example bellow. Note that all views, ConnectingLine, originView and destinationView, should be inside the same ViewGroup and that ConnectingLine works only inside a ConstraintLayout.
 
	<com.gallardo.widget.ConnectingLineView
		android:id="@+id/five_to_one"
		android:layout_width="wrap_content"
		android:layout_height="wrap_content"
		app:originView="@id/origin"
		app:destinationView="@id/destination"
		app:lineWidth="10"
		app:lineColor="#050505"
		app:shadowColor="#050505"
		app:cornerEffectRadius="30"
		app:dentSize="50"
		tools:ignore="MissingConstraints" />

You can also include a ConnectingLine programmatically:

	//Create a new ConnectingLine passing the context, origin view and destination view ids.
	val connectLine = ConnectingLineView(context, R.id.origin, R.id.destination)
  
	//Find the parent Constraint Layout
	val parent = findViewById<ConstraintLayout>(R.id.constraintLayout)
  
	//Add the ConnectingLine to the parent
	parent.addView(connectLine)

	//Edit ConnectingLine visuals
	connectLine.dentSize = 15
	connectLine.preferredPath = ConnectingLineView.LEFT_TO_TOP
	var paint = Paint(Paint.ANTI_ALIAS_FLAG)
	paint.color = Color.BLACK
	paint.strokeWidth = 5f
	paint.pathEffect = CornerPathEffect(5f)
	paint.setShadowLayer(2f, 1f, 1f, Color.YELLOW)
	connectLine.paint = paint
