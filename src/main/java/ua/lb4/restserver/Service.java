package ua.lb4.restserver;

import ua.lb4.restserver.model.driver.Driver;
import ua.lb4.restserver.model.order.Order;
import ua.lb4.restserver.model.order.Orders;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.*;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/")
public class Service {

    private static Logger log;

    static {log = Logger.getLogger(Service.class.getName());}

    private Orders unMarshalOrders() {
        File file = new File(LINK.ORDERS_FILE);
        if (file.canRead()) {
            try {
                Unmarshaller um = JAXBContext.newInstance(ua.lb4.restserver.model.order.ObjectFactory.class).createUnmarshaller();
                return (Orders) um.unmarshal(file);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
        return new Orders();
    }
    @POST
    @Path("sendOrder")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Order createOrder(Order order) {
        Orders orders = unMarshalOrders();
        orders.getOrder().add(order);
        try {
            Marshaller marshaller = JAXBContext.newInstance(ua.lb4.restserver.model.order.ObjectFactory.class).createMarshaller();
            JAXBElement<Orders> jorder = new JAXBElement<>(
                    new QName(LINK.ORDER, "orders"),
                    Orders.class, orders);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(jorder, new File(LINK.ORDERS_FILE));
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        log.info("Було додано нове замовлення, перевірити можна у файлі orders.xml\n");
        return orders.getOrder().get(orders.getOrder().size() - 1);
    }
    @GET
    @Path("userHistory/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Orders getHistory(@PathParam("id") int id) {
        Orders orders = unMarshalOrders();
        Orders ordersToReturn = new Orders();
        orders.getOrder().forEach(o -> {
            if (o.getUser().getId() == id) ordersToReturn.getOrder().add(o);
        });
        log.info("Було надано історію користувача з ID: " + id+"\n");
        return ordersToReturn;
    }
    @GET
    @Path("driverBySurname/{surname}")
    @Produces(MediaType.APPLICATION_XML)
    public List<Driver> getDriversBySurname(@PathParam("surname") String surname) {
        log.info("Пошук водія за прізвищем: " + surname+"\n");
        return unMarshalOrders().getOrder().stream().map(Order::getDriver).filter(driver -> driver.getSurname().equals(surname)).collect(Collectors.toList());
    }
    @GET
    @Path("driverByCode/{code}")
    @Produces(MediaType.APPLICATION_XML)
    public List<Driver> getDriverByCode(@PathParam("code") String code) {
        log.info("Пошук водія за кодом: " + code+"\n");
        return unMarshalOrders().getOrder().stream().map(Order::getDriver).filter(driver -> driver.getCode().equals(code)).collect(Collectors.toList());
    }
    @GET
    @Path("historyAbove/{mark}")
    @Produces(MediaType.APPLICATION_XML)
    public Orders getHistoryAboveMark(@PathParam("mark") int mark) {
        Orders toReturn = new Orders();
        unMarshalOrders().getOrder().stream().filter(o -> o.getMark() > mark).forEach(order -> toReturn.getOrder().add(order));
        log.info("Отриманя історії з оцінкою вище за " + mark+"\n");
        return toReturn;
    }
    @GET
    @Path("historyBelow/{mark}")
    @Produces(MediaType.APPLICATION_XML)
    public Orders getHistoryBelowMark(@PathParam("mark") int mark) {
        Orders toReturn = new Orders();
        unMarshalOrders().getOrder().stream().filter(o -> o.getMark() < mark).forEach(order -> toReturn.getOrder().add(order));
        log.info("Отриманя історії з оцінкою нижче за " + mark+"\n");
        return toReturn;
    }
}
