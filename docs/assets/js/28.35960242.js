(window.webpackJsonp=window.webpackJsonp||[]).push([[28],{306:function(t,s,a){"use strict";a.r(s);var n=a(14),e=Object(n.a)({},(function(){var t=this,s=t._self._c;return s("ContentSlotsDistributor",{attrs:{"slot-key":t.$parent.slotKey}},[s("h2",{attrs:{id:"概述"}},[s("a",{staticClass:"header-anchor",attrs:{href:"#概述"}},[t._v("#")]),t._v(" 概述")]),t._v(" "),s("p",[t._v("通过在方法或类上添加 "),s("code",[t._v("@ContainerMethod")]),t._v(" 注解，可以将任意方法适配为方法数据源容器 "),s("code",[t._v("MethodInvokerContainer")]),t._v("。当调用容器时，方法将自动执行，并将方法执行结果作为数据源对象返回。")]),t._v(" "),s("h2",{attrs:{id:"_2-5-1-声明方法数据源"}},[s("a",{staticClass:"header-anchor",attrs:{href:"#_2-5-1-声明方法数据源"}},[t._v("#")]),t._v(" 2.5.1.声明方法数据源")]),t._v(" "),s("p",[s("strong",[t._v("直接声明")])]),t._v(" "),s("p",[t._v("可以直接在类上添加 "),s("code",[t._v("@ContainerMethod")]),t._v(" 注解，将方法声明为数据源。")]),t._v(" "),s("div",{staticClass:"language-java extra-class"},[s("pre",{pre:!0,attrs:{class:"language-java"}},[s("code",[s("span",{pre:!0,attrs:{class:"token annotation punctuation"}},[t._v("@ContainerMethod")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("(")]),t._v("\n    namespace "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token string"}},[t._v('"onoToOneMethod"')]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v("\n    resultType "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Foo")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(".")]),s("span",{pre:!0,attrs:{class:"token keyword"}},[t._v("class")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v(" resultKey "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token string"}},[t._v('"id"')]),t._v(" "),s("span",{pre:!0,attrs:{class:"token comment"}},[t._v("// 返回的数据源对象类型为 Foo，并且需要按 id 分组")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(")")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token keyword"}},[t._v("public")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Set")]),s("span",{pre:!0,attrs:{class:"token generics"}},[s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("<")]),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Foo")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(">")])]),t._v(" "),s("span",{pre:!0,attrs:{class:"token function"}},[t._v("onoToOneMethod")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("(")]),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("List")]),s("span",{pre:!0,attrs:{class:"token generics"}},[s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("<")]),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("String")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(">")])]),t._v(" args"),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(")")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("{")]),t._v("\n    "),s("span",{pre:!0,attrs:{class:"token comment"}},[t._v("// do something")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("}")]),t._v("\n")])])]),s("p",[t._v("在 Spring 环境中，当项目启动时，在后处理阶段会扫描该方法并将其注册为命名空间为 "),s("code",[t._v("onoToOneMethod")]),t._v(" 的数据源容器。")]),t._v(" "),s("div",{staticClass:"custom-block tip"},[s("p",{staticClass:"custom-block-title"},[t._v("TIP")]),t._v(" "),s("p",[s("code",[t._v("resultKey")]),t._v(" 默认支持链式操作符，可以通过 "),s("code",[t._v("xx.xx.xx")]),t._v(" 的方式访问内部对象的属性。")])]),t._v(" "),s("p",[s("strong",[t._v("间接声明")])]),t._v(" "),s("p",[t._v("还可以在类上声明方法数据源，但需要额外使用 "),s("code",[t._v("bindMethod")]),t._v(" 属性进行方法绑定。例如：")]),t._v(" "),s("div",{staticClass:"language-java extra-class"},[s("pre",{pre:!0,attrs:{class:"language-java"}},[s("code",[s("span",{pre:!0,attrs:{class:"token comment"}},[t._v("// 父类")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token keyword"}},[t._v("public")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token keyword"}},[t._v("class")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("SuperClass")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("{")]),t._v("\n    "),s("span",{pre:!0,attrs:{class:"token keyword"}},[t._v("public")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Set")]),s("span",{pre:!0,attrs:{class:"token generics"}},[s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("<")]),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Foo")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(">")])]),t._v(" "),s("span",{pre:!0,attrs:{class:"token function"}},[t._v("onoToOneMethod")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("(")]),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("List")]),s("span",{pre:!0,attrs:{class:"token generics"}},[s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("<")]),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("String")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(">")])]),t._v(" args"),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(")")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("{")]),t._v("\n        "),s("span",{pre:!0,attrs:{class:"token comment"}},[t._v("// do something")]),t._v("\n    "),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("}")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("}")]),t._v("\n\n"),s("span",{pre:!0,attrs:{class:"token comment"}},[t._v("// 子类")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token annotation punctuation"}},[t._v("@ContainerMethod")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("(")]),t._v("\n    namespace "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token string"}},[t._v('"onoToOneMethod"')]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v("\n    resultType "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Foo")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(".")]),s("span",{pre:!0,attrs:{class:"token keyword"}},[t._v("class")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v(" resultKey "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token string"}},[t._v('"id"')]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token comment"}},[t._v("// 返回的数据源对象类型为 Foo，并且需要按 id 分组")]),t._v("\n    bindMethod "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token string"}},[t._v('"onoToOneMethod"')]),t._v(" "),s("span",{pre:!0,attrs:{class:"token comment"}},[t._v("// 指定方法名称，如果存在重载方法，也可以额外的指明要绑定的方法的参数类型")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(")")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token keyword"}},[t._v("public")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token keyword"}},[t._v("class")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("ChildClass")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token keyword"}},[t._v("extends")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("SuperClass")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("{")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("}")]),t._v("\n")])])]),s("p",[t._v("在上述示例中，我们在 "),s("code",[t._v("ChildClass")]),t._v(" 中将父类 "),s("code",[t._v("SuperClass")]),t._v(" 中的 "),s("code",[t._v("onoToOneMethod")]),t._v(" 方法声明为方法数据源容器。")]),t._v(" "),s("p",[s("strong",[t._v("可以作为数据源源的方法可以/需要具备下述特征")]),t._v("：")]),t._v(" "),s("ul",[s("li",[t._v("可以是实例方法或静态方法；")]),t._v(" "),s("li",[t._v("可以是无参方法或有参方法，如果是有参方法，则第一个参数必须是 "),s("code",[t._v("Collection")]),t._v(" 类型；")]),t._v(" "),s("li",[t._v("方法必须有返回值，可以是与 "),s("code",[t._v("resultType")]),t._v(" 类型对应的对象、对象数组或集合，或者是 "),s("code",[t._v("Map")]),t._v(" 集合；")])]),t._v(" "),s("h2",{attrs:{id:"_2-5-2-对结果分组"}},[s("a",{staticClass:"header-anchor",attrs:{href:"#_2-5-2-对结果分组"}},[t._v("#")]),t._v(" 2.5.2.对结果分组")]),t._v(" "),s("p",[t._v("由于数据源容器的返回值需要按照 key 分组，因此注解必须通过 "),s("code",[t._v("resultType")]),t._v(" 和 "),s("code",[t._v("resultKey")]),t._v(" 来指定获取数据源对象后用于分组的 key 字段。")]),t._v(" "),s("p",[t._v("一般情况下，方法数据源容器返回的对象与 key 是一对一的关系，这是默认的情况（"),s("code",[t._v("MappingType.ONE_TO_ONE")]),t._v("）。不过，我们可以通过 "),s("code",[t._v("type")]),t._v(" 属性来指定映射类型，从而改变结果的分组方式。")]),t._v(" "),s("h3",{attrs:{id:"_2-5-2-1-一对一"}},[s("a",{staticClass:"header-anchor",attrs:{href:"#_2-5-2-1-一对一"}},[t._v("#")]),t._v(" 2.5.2.1.一对一")]),t._v(" "),s("p",[t._v("即一个输入的 key 值对应一个数据源对象，类型为 "),s("code",[t._v("MappingType.ONE_TO_ONE")]),t._v("，这是默认的情况。")]),t._v(" "),s("h3",{attrs:{id:"_2-5-2-2-一对多"}},[s("a",{staticClass:"header-anchor",attrs:{href:"#_2-5-2-2-一对多"}},[t._v("#")]),t._v(" 2.5.2.2.一对多")]),t._v(" "),s("p",[t._v("即一个输入的 key 值对应一个数据源集合，类型为 "),s("code",[t._v("MappingType.ONE_TO_MANY")]),t._v("。")]),t._v(" "),s("p",[t._v("例如，如果有一个方法根据 "),s("code",[t._v("classId")]),t._v(" 获取 "),s("code",[t._v("student")]),t._v(" 对象，希望返回的 "),s("code",[t._v("student")]),t._v(" 对象可以按照 "),s("code",[t._v("classId")]),t._v(" 分组：")]),t._v(" "),s("div",{staticClass:"language-java extra-class"},[s("pre",{pre:!0,attrs:{class:"language-java"}},[s("code",[s("span",{pre:!0,attrs:{class:"token annotation punctuation"}},[t._v("@ContainerMethod")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("(")]),t._v("\n    namespace "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token string"}},[t._v('"student-class"')]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v("\n    resultType "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Student")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(".")]),s("span",{pre:!0,attrs:{class:"token keyword"}},[t._v("class")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v(" resultKey "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token string"}},[t._v('"classId"')]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token comment"}},[t._v("// 返回的数据源对象类型为 Student，并且需要按 classId 分组")]),t._v("\n    type "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("MappingType")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(".")]),s("span",{pre:!0,attrs:{class:"token constant"}},[t._v("ONE_TO_MANY")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token comment"}},[t._v("// 返回的数据源对象与待处理对象类型为一对多，即一个处理对象的 key 值对应一个数据源对象的 key")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(")")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token keyword"}},[t._v("public")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("List")]),s("span",{pre:!0,attrs:{class:"token generics"}},[s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("<")]),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Student")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(">")])]),t._v(" "),s("span",{pre:!0,attrs:{class:"token function"}},[t._v("getStudentByClassIds")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("(")]),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("List")]),s("span",{pre:!0,attrs:{class:"token generics"}},[s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("<")]),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Integer")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(">")])]),t._v(" classIds"),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(")")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("{")]),t._v("\n    "),s("span",{pre:!0,attrs:{class:"token comment"}},[t._v("// do something")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("}")]),t._v("\n")])])]),s("p",[t._v("该方法适配为数据源容器后，将接受 "),s("code",[t._v("classId")]),t._v(" 集合，并返回按 "),s("code",[t._v("classId")]),t._v(" 分组的 "),s("code",[t._v("student")]),t._v(" 对象（"),s("code",[t._v("Map<Integer, List<Student>>")]),t._v("）。")]),t._v(" "),s("h3",{attrs:{id:"_2-5-2-3-不分组"}},[s("a",{staticClass:"header-anchor",attrs:{href:"#_2-5-2-3-不分组"}},[t._v("#")]),t._v(" 2.5.2.3.不分组")]),t._v(" "),s("p",[t._v("有时候，方法返回值已经是按需分组的 "),s("code",[t._v("Map")]),t._v(" 集合，此时可以指定类型为 "),s("code",[t._v("MappingType.MAPPED")]),t._v("，表示方法已经完成了映射，无需进行额外的分组。")]),t._v(" "),s("div",{staticClass:"language-java extra-class"},[s("pre",{pre:!0,attrs:{class:"language-java"}},[s("code",[s("span",{pre:!0,attrs:{class:"token annotation punctuation"}},[t._v("@ContainerMethod")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("(")]),t._v("namespace "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token string"}},[t._v('"student"')]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v(" type "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("MappingType")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(".")]),s("span",{pre:!0,attrs:{class:"token constant"}},[t._v("MAPPED")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(")")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token keyword"}},[t._v("public")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Map")]),s("span",{pre:!0,attrs:{class:"token generics"}},[s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("<")]),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Integer")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Student")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(">")])]),t._v(" "),s("span",{pre:!0,attrs:{class:"token function"}},[t._v("getStudentByIds")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("(")]),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("List")]),s("span",{pre:!0,attrs:{class:"token generics"}},[s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("<")]),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Integer")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(">")])]),t._v(" ids"),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(")")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("{")]),t._v("\n    "),s("span",{pre:!0,attrs:{class:"token comment"}},[t._v("// do something")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("}")]),t._v("\n")])])]),s("p",[t._v("在上述示例中，"),s("code",[t._v("getStudentByIds")]),t._v(" 方法返回值已经是按 "),s("code",[t._v("id")]),t._v(" 分组的 "),s("code",[t._v("Student")]),t._v(" 对象集合，因此无需进行额外的分组，也不需要指定 "),s("code",[t._v("resultType")]),t._v(" 和 "),s("code",[t._v("resultKey")]),t._v("。")]),t._v(" "),s("h2",{attrs:{id:"_2-5-4-数据源容器工厂"}},[s("a",{staticClass:"header-anchor",attrs:{href:"#_2-5-4-数据源容器工厂"}},[t._v("#")]),t._v(" 2.5.4.数据源容器工厂")]),t._v(" "),s("p",[t._v("类似于 "),s("code",[t._v("Spring")]),t._v(" 处理 "),s("code",[t._v("@EventListener")]),t._v(" 注解，"),s("code",[t._v("cranej4")]),t._v(" 通过基于注解处理器 "),s("code",[t._v("MethodContainerAnnotationProcessor")]),t._v(" 处理带有 "),s("code",[t._v("@ContainerMethod")]),t._v(" 注解的方法。它会选择优先级最高的数据源容器工厂 "),s("code",[t._v("MethodContainerFactory")]),t._v(" 将带有注解的方法适配为数据源容器。")]),t._v(" "),s("p",[s("code",[t._v("crane4j")]),t._v(" 默认提供了两种工厂："),s("code",[t._v("DefaultMethodContainerFactory")]),t._v(" 和 "),s("code",[t._v("CacheableMethodContainerFactory")]),t._v("。分别用于处理带有 "),s("code",[t._v("@ContainerMethod")]),t._v(" 注解的方法和带有 "),s("code",[t._v("@ContainerCache")]),t._v(" 注解的方法。")]),t._v(" "),s("p",[t._v("用户也可以通过实现 "),s("code",[t._v("MethodContainerFactory")]),t._v(" 接口并提高优先级，然后将其声明为 Spring 上下文的 bean 自动注册，或手动将其注册到注解处理器中，以实现自定义逻辑。")]),t._v(" "),s("h2",{attrs:{id:"_2-5-5-注册方法数据源容器"}},[s("a",{staticClass:"header-anchor",attrs:{href:"#_2-5-5-注册方法数据源容器"}},[t._v("#")]),t._v(" 2.5.5.注册方法数据源容器")]),t._v(" "),s("p",[t._v("在 Spring 环境中，只要方法所在的 bean 被 Spring 管理，这些方法就会自动适配为方法数据源容器，并在全局配置中注册。")]),t._v(" "),s("p",[t._v("在非 Spring 环境中，可以使用 "),s("code",[t._v("MethodContainerAnnotationProcessor")]),t._v(" 手动扫描类中的方法，并创建方法数据源容器：")]),t._v(" "),s("div",{staticClass:"language-java extra-class"},[s("pre",{pre:!0,attrs:{class:"language-java"}},[s("code",[s("span",{pre:!0,attrs:{class:"token comment"}},[t._v("// 配置反射工厂与注解查找器")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Crane4jGlobalConfiguration")]),t._v(" configuration "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("SimpleCrane4jGlobalConfiguration")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(".")]),s("span",{pre:!0,attrs:{class:"token function"}},[t._v("create")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("(")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(")")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(";")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("PropertyOperator")]),t._v(" propertyOperator "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" configuration"),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(".")]),s("span",{pre:!0,attrs:{class:"token function"}},[t._v("getPropertyOperator")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("(")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(")")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(";")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("AnnotationFinder")]),t._v(" annotationFinder "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token keyword"}},[t._v("new")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("SimpleAnnotationFinder")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("(")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(")")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(";")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token comment"}},[t._v("// 配置方法数据源工厂")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Collection")]),s("span",{pre:!0,attrs:{class:"token generics"}},[s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("<")]),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("MethodContainerFactory")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(">")])]),t._v(" factories "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Arrays")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(".")]),s("span",{pre:!0,attrs:{class:"token function"}},[t._v("asList")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("(")]),t._v("\n    "),s("span",{pre:!0,attrs:{class:"token keyword"}},[t._v("new")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("DefaultMethodContainerFactory")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("(")]),t._v("propertyOperator"),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v(" annotationFinder"),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(")")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(")")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(";")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token comment"}},[t._v("// 扫描类中的方法，创建方法数据源容器")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("MethodContainerAnnotationProcessor")]),t._v(" processor "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token keyword"}},[t._v("new")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("MethodContainerAnnotationProcessor")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("(")]),s("span",{pre:!0,attrs:{class:"token keyword"}},[t._v("new")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("SimpleAnnotationFinder")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("(")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(")")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v(" factories"),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(")")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(";")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Collection")]),s("span",{pre:!0,attrs:{class:"token generics"}},[s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("<")]),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Container")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("<")]),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Object")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(">")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(">")])]),t._v(" containers "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" processor"),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(".")]),s("span",{pre:!0,attrs:{class:"token function"}},[t._v("process")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("(")]),t._v("foo"),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Foo")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(".")]),s("span",{pre:!0,attrs:{class:"token function"}},[t._v("getClass")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("(")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(")")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(")")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(";")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token comment"}},[t._v("// 获取全局上下文并注册容器")]),t._v("\n"),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Crane4jGlobalConfiguration")]),t._v(" configuration "),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v(" "),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("SpringUtils")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(".")]),s("span",{pre:!0,attrs:{class:"token function"}},[t._v("getBean")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("(")]),s("span",{pre:!0,attrs:{class:"token class-name"}},[t._v("Crane4jGlobalConfiguration")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(".")]),s("span",{pre:!0,attrs:{class:"token keyword"}},[t._v("class")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(")")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(";")]),t._v("\ncontainers"),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(".")]),s("span",{pre:!0,attrs:{class:"token function"}},[t._v("forEach")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("(")]),t._v("configuration"),s("span",{pre:!0,attrs:{class:"token operator"}},[t._v("::")]),s("span",{pre:!0,attrs:{class:"token function"}},[t._v("registerContainer")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(")")]),s("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(";")]),t._v("\n")])])]),s("p",[t._v("以上代码配置了反射工厂与注解查找器，并创建了方法数据源工厂集合。然后使用注解处理器 "),s("code",[t._v("MethodContainerAnnotationProcessor")]),t._v(" 扫描类中的方法，并创建方法数据源容器。最后，将创建的容器注册到全局配置中。")])])}),[],!1,null,null,null);s.default=e.exports}}]);