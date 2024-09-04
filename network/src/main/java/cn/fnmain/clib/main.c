int l_epoll_create() {
    int r = epoll_create(1);
    if (r == -1) {
        return -errno;
    }
    return r;
}

int l_ipv4_socket_create() {
   int r = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
   if (r == -1) {
        return -errno;
   }
   return r;
}

int l_ipv6_socket_create() {
    int r = socket(AF_INET6, SOCK_STREAM, IPPROTO_TCP);
    if (r == -1) {
        return -errno;
    }
    return r;
}