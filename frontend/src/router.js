
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import RoomManager from "./components/RoomManager"

import ReservationManager from "./components/ReservationManager"

import PaymentManager from "./components/PaymentManager"


import Reservationview from "./components/Reservationview"
export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/rooms',
                name: 'RoomManager',
                component: RoomManager
            },

            {
                path: '/reservations',
                name: 'ReservationManager',
                component: ReservationManager
            },

            {
                path: '/payments',
                name: 'PaymentManager',
                component: PaymentManager
            },


            {
                path: '/reservationviews',
                name: 'Reservationview',
                component: Reservationview
            },


    ]
})
