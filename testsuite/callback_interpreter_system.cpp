#include <boost/test/unit_test.hpp>

#include <boost/thread.hpp>
#include <boost/bind.hpp>

#include "../source/utilities/callback_system.hpp"
#include "../source/utilities/callback_interpreter.hpp"
#include "nova-tt/thread_priority.hpp"

using namespace nova;

namespace
{

int i = 0;

struct dummy
{
    void run(void)
    {
        ++i;
    }
};

}

BOOST_AUTO_TEST_CASE( callback_system_test )
{
    callback_system<dummy> cbs;

    cbs.add_callback(new dummy());
    cbs.run_callbacks();
    BOOST_REQUIRE_EQUAL(i, 1);
}

BOOST_AUTO_TEST_CASE( callback_interpreter_test )
{
    callback_interpreter<dummy> cbi;

    boost::thread thread = cbi.start_thread();

    for (int j = 0; j != 20; ++j)
        cbi.add_callback(new dummy());

    cbi.terminate();
    thread.join();
    BOOST_REQUIRE_EQUAL(i, 21);
}

BOOST_AUTO_TEST_CASE( callback_interpreter_threadpool_test )
{
    {
        callback_interpreter_threadpool<dummy> cbi(4, true, thread_priority_interval().first);

        for (int j = 0; j != 20; ++j)
            cbi.add_callback(new dummy());
    }
    BOOST_REQUIRE_EQUAL(i, 41);
}
