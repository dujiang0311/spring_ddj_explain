/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.xml;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.Resource;

/**
 * Convenience extension of {@link DefaultListableBeanFactory} that reads bean definitions
 * from an XML document. Delegates to {@link XmlBeanDefinitionReader} underneath; effectively
 * equivalent to using an XmlBeanDefinitionReader with a DefaultListableBeanFactory.
 *
 * <p>The structure, element and attribute names of the required XML document
 * are hard-coded in this class. (Of course a transform could be run if necessary
 * to produce this format). "beans" doesn't need to be the root element of the XML
 * document: This class will parse all bean definition elements in the XML file.
 *
 * <p>This class registers each bean definition with the {@link DefaultListableBeanFactory}
 * superclass, and relies on the latter's implementation of the {@link BeanFactory} interface.
 * It supports singletons, prototypes, and references to either of these kinds of bean.
 * See {@code "spring-beans-3.x.xsd"} (or historically, {@code "spring-beans-2.0.dtd"}) for
 * details on options and configuration style.
 *
 * <p><b>For advanced needs, consider using a {@link DefaultListableBeanFactory} with
 * an {@link XmlBeanDefinitionReader}.</b> The latter allows for reading from multiple XML
 * resources and is highly configurable in its actual XML parsing behavior.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 15 April 2001
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 * @see XmlBeanDefinitionReader
 * @deprecated as of Spring 3.1 in favor of {@link DefaultListableBeanFactory} and
 * {@link XmlBeanDefinitionReader}
 */
@Deprecated
@SuppressWarnings({"serial", "all"})
public class XmlBeanFactory extends DefaultListableBeanFactory {
// ddj_006:XML 配置文件的读取是Spring中的一个重要的功能，读取XML配置就是用XmlBeanDefinitionReader 主要是做了资源文件读取、解析及注册的大致脉络
// ddj_007：一开始看到这个类还是有点儿懵逼的，为什么要从这块开始阅读，后来我们在写Spring 使用的例子时看到了这样一句话，BeanFactory bf = new XmlBeanFactory(new ClassPathResource("beanFactoryTest.xml"));
// 看这个代码的时序图（时序图安装插件：https://blog.csdn.net/iiiliuyang/article/details/121482837）其实不看也行，代码一般都是从上到下，从左到右，从里到外执行的，我们在研究XmlBeanFactory 之前，先研究研究里面的东西 new ClassPathResource("beanFactoryTest.xml")
	private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);

	// 还记得吗，一个类可以有多个构造函数，客户端用哪个，一般由入参决定，那我们ddj_007 里面的代码应该是走了下面这个构造函数里去了
	/**
	 * Create a new XmlBeanFactory with the given resource,
	 * which must be parsable using DOM.
	 * @param resource XML resource to load bean definitions from
	 * @throws BeansException in case of loading or parsing errors
	 */
	public XmlBeanFactory(Resource resource) throws BeansException {
		// ddj_010 从这儿进去，构造函数再次调用内部构造函数
		this(resource, null);
	}

	/**
	 * Create a new XmlBeanFactory with the given input stream,
	 * which must be parsable using DOM.
	 * @param resource XML resource to load bean definitions from
	 * @param parentBeanFactory parent bean factory
	 * @throws BeansException in case of loading or parsing errors
	 */
	public XmlBeanFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
		// ddj_011 在资源架子啊之前，还需要调用父类构造函数初始化的过程，点进去看看
		super(parentBeanFactory);
		// ddj_015 终于找到资源加载的真正实现了，我们在读代码要明白一件事，Spring 源码也是人写的，再满足架构设计的基础上写代码，必然存在先写主干，再补逻辑，也就是不可能一开始就把所有功能都想的很健壮，但是我们在阅读源码的时候，一定是一个很健壮的代码，
		// 也是经过很多迭代才产出的，所以我们要找到核心主干，然后学习其中的设计模式，甚至于方法明明方式，异常处理方式
		// ddj_016 先看下这个方法的时序图，当然，大概看看就行，绕的人头皮发麻，直接把关键点走下把
		this.reader.loadBeanDefinitions(resource);
	}

}
