from __future__ import print_function

import time
from dronekit import connect, VehicleMode, LocationGlobalRelative
from nose.tools import assert_equals

from pyspookystuff.mav import arm_and_takeoff
from pyspookystuff.mav_test import with_sitl_3way

@with_sitl_3way
def test_ferry(connpath):
    vehicle = connect(connpath, wait_ready=True)

    # NOTE these are *very inappropriate settings*
    # to make on a real vehicle. They are leveraged
    # exclusively for simulation. Take heed!!!
    vehicle.parameters['FS_GCS_ENABLE'] = 0
    vehicle.parameters['FS_EKF_THRESH'] = 100

    arm_and_takeoff(20, vehicle)

    point1 = LocationGlobalRelative(90, 0, 20)

    print("Going to first point...")
    vehicle.simple_goto(point1)

    # sleep so we can see the change in map
    time.sleep(3000000)

    print("Returning to Launch")
    vehicle.mode = VehicleMode("RTL")

    vehicle.close()
