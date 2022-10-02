package nextstep.study.di.stage3.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 스프링의 BeanFactory, ApplicationContext에 해당되는 클래스
 */
class DIContainer {

    private final Set<Object> beans;

    public DIContainer(final Set<Class<?>> classes) {
        this.beans = classes.stream()
                .map(this::initiateBean)
                .collect(Collectors.toSet());
        this.beans.forEach(bean -> setFields(bean.getClass(), bean));
    }

    private Object initiateBean(final Class<?> aClass) {
        final Constructor<?> constructor;
        try {
            constructor = aClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException |
                 InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setFields(final Class<?> aClass, final Object bean) {
        for (Field field : aClass.getDeclaredFields()) {
            try {
                setField(bean, field);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void setField(Object bean, Field field) throws IllegalAccessException {
        Optional<Object> object = beans.stream()
                .filter(obj -> field.getType().isAssignableFrom(obj.getClass()))
                .findFirst();
        field.setAccessible(true);
        if (object.isPresent()) {
            field.set(bean, object.get());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(final Class<T> aClass) {
        return (T) beans.stream()
                .filter(bean -> bean.getClass().equals(aClass))
                .findFirst()
                .orElseThrow();
    }
}
