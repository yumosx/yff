package com.MiniSpring.context;


import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.MiniSpring.beans.BeanDefinition;


public class ClassPathXmlApplicationContext {
    private List<BeanDefinition> beanDefinitions = new ArrayList<BeanDefinition>();
    private Map<String, Object> singletons = new HashMap<>();


    //构造器获取外部配置, 解析出来Bean
    public ClassPathXmlApplicationContext(String filename) {
        this.readXml(filename);
    }

    public void readXml(String filename) {
        SAXReader saxReader = new SAXReader();

        try {
            //这里就实现一种元操作
            Class thisClass = this.getClass();

            URL xmlPath = thisClass.getClassLoader().getResource(filename);
            Document document = saxReader.read(xmlPath);
            Element rootElement = document.getRootElement();    //类似于dom节点

            //对配置文件中的每一个<bean>进行处理
            for (Element element : (List<Element>) rootElement.elements()) {
                //获取bean的基本信息
                String beanID = element.attributeValue("id");
                String beanClassName = element.attributeValue("class");
                BeanDefinition beanDefinition = new BeanDefinition(beanID, beanClassName);

                //保存
                beanDefinitions.add(beanDefinition);
            }

        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    //利用反射创建bean实例, 并存储在对应的实例中
    private void instanceBeans() throws ClassNotFoundException {
        for (BeanDefinition beanDefinition : beanDefinitions) {
            try {
                singletons.put(beanDefinition.getId(), Class.forName(beanDefinition.getClassName()).newInstance());
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //根据singletons创建实例
    public Object getBean(String beanName) {
        return singletons.get(beanName);
    }


}
