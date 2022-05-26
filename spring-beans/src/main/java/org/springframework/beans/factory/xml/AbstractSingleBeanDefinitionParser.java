/*
 * Copyright 2002-2017 the original author or authors.
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

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.lang.Nullable;

/**
 * Base class for those {@link BeanDefinitionParser} implementations that
 * need to parse and define just a <i>single</i> {@code BeanDefinition}.
 *
 * <p>Extend this parser class when you want to create a single bean definition
 * from an arbitrarily complex XML element. You may wish to consider extending
 * the {@link AbstractSimpleBeanDefinitionParser} when you want to create a
 * single bean definition from a relatively simple custom XML element.
 *
 * <p>The resulting {@code BeanDefinition} will be automatically registered
 * with the {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}.
 * Your job simply is to {@link #doParse parse} the custom XML {@link Element}
 * into a single {@code BeanDefinition}.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2.0
 * @see #getBeanClass
 * @see #getBeanClassName
 * @see #doParse
 */
public abstract class AbstractSingleBeanDefinitionParser extends AbstractBeanDefinitionParser {

	/**
	 * Creates a {@link BeanDefinitionBuilder} instance for the
	 * {@link #getBeanClass bean Class} and passes it to the
	 * {@link #doParse} strategy method.
	 * @param element the element that is to be parsed into a single BeanDefinition
	 * @param parserContext the object encapsulating the current state of the parsing process
	 * @return the BeanDefinition resulting from the parsing of the supplied {@link Element}
	 * @throws IllegalStateException if the bean {@link Class} returned from
	 * {@link #getBeanClass(org.w3c.dom.Element)} is {@code null}
	 * @see #doParse
	 */
	@Override
	protected final AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
		String parentName = getParentName(element);
		if (parentName != null) {
			builder.getRawBeanDefinition().setParentName(parentName);
		}
		// ddj_090 获取自定义比标签中的class ，此时就会调用自定义解析器（真滴是自己手写的那个类哟）中的getClass 方法
		Class<?> beanClass = getBeanClass(element);
		if (beanClass != null) {
			builder.getRawBeanDefinition().setBeanClass(beanClass);
		}
		else {
			// ddj_091 如果没有重写 getBeanClass 方法，那就看看有没有重写 getBeanClassName 方法
			String beanClassName = getBeanClassName(element);
			if (beanClassName != null) {
				builder.getRawBeanDefinition().setBeanClassName(beanClassName);
			}
		}
		builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));
		BeanDefinition containingBd = parserContext.getContainingBeanDefinition();
		if (containingBd != null) {
			// Inner bean definition must receive same scope as containing bean.
			// ddj_092 若存在父类，则用父类的scope 属性
			builder.setScope(containingBd.getScope());
		}
		if (parserContext.isDefaultLazyInit()) {
			// Default-lazy-init applies to custom bean definitions as well.
			// ddj_093 配置延迟加载
			builder.setLazyInit(true);
		}
		// ddj_094 这里就是调用子类重写的 doParse 方法了，你在dubbo 的自定义解析器里面其实都可以看到这些方法，他都实现了。
		// ddj_095 总结下，虽然我们自定义了解析器，继承了AbstractSingleBeanDefinitionParser 这个类，但是我们只是单纯的做了和自己业务逻辑相关的部分。其实这个处理过程和spring 默认标签的处理方式基本差不多，
		// 包括创建的 BeanDefinition 以及上面做的一些默认属性的设置，至此，我们已经搞清楚了 spring 的全部解析工作，也就是说我们能理解spring 是如何将配置文件（*.xml） 里的 bean 加载到内存中的全过程，接下
		// 来就是如何使用了，也就是 bean 的加载。
		doParse(element, parserContext, builder);
		return builder.getBeanDefinition();
	}

	/**
	 * Determine the name for the parent of the currently parsed bean,
	 * in case of the current bean being defined as a child bean.
	 * <p>The default implementation returns {@code null},
	 * indicating a root bean definition.
	 * @param element the {@code Element} that is being parsed
	 * @return the name of the parent bean for the currently parsed bean,
	 * or {@code null} if none
	 */
	@Nullable
	protected String getParentName(Element element) {
		return null;
	}

	/**
	 * Determine the bean class corresponding to the supplied {@link Element}.
	 * <p>Note that, for application classes, it is generally preferable to
	 * override {@link #getBeanClassName} instead, in order to avoid a direct
	 * dependence on the bean implementation class. The BeanDefinitionParser
	 * and its NamespaceHandler can be used within an IDE plugin then, even
	 * if the application classes are not available on the plugin's classpath.
	 * @param element the {@code Element} that is being parsed
	 * @return the {@link Class} of the bean that is being defined via parsing
	 * the supplied {@code Element}, or {@code null} if none
	 * @see #getBeanClassName
	 */
	@Nullable
	protected Class<?> getBeanClass(Element element) {
		return null;
	}

	/**
	 * Determine the bean class name corresponding to the supplied {@link Element}.
	 * @param element the {@code Element} that is being parsed
	 * @return the class name of the bean that is being defined via parsing
	 * the supplied {@code Element}, or {@code null} if none
	 * @see #getBeanClass
	 */
	@Nullable
	protected String getBeanClassName(Element element) {
		return null;
	}

	/**
	 * Parse the supplied {@link Element} and populate the supplied
	 * {@link BeanDefinitionBuilder} as required.
	 * <p>The default implementation delegates to the {@code doParse}
	 * version without ParserContext argument.
	 * @param element the XML element being parsed
	 * @param parserContext the object encapsulating the current state of the parsing process
	 * @param builder used to define the {@code BeanDefinition}
	 * @see #doParse(Element, BeanDefinitionBuilder)
	 */
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		doParse(element, builder);
	}

	/**
	 * Parse the supplied {@link Element} and populate the supplied
	 * {@link BeanDefinitionBuilder} as required.
	 * <p>The default implementation does nothing.
	 * @param element the XML element being parsed
	 * @param builder used to define the {@code BeanDefinition}
	 */
	protected void doParse(Element element, BeanDefinitionBuilder builder) {
	}

}
